package org.whsv26.tapir

import config.Config.AppConfig
import domain.auth.{AuthService, JwtClockAlg}
import domain.foos.{FooService, FooValidationInterpreter}
import infrastructure.auth.{BCryptHasherInterpreter, JwtTokenInterpreter}
import infrastructure.endpoint.foos.{CreateFooEndpoint, DeleteFooEndpoint, GetFooEndpoint}
import infrastructure.endpoint.jwt.CreateJwtTokenEndpoint
import infrastructure.messaging.kafka.{DeleteFooConsumer, DeleteFooProducer}
import infrastructure.repository.inmemory.MemoryUserRepositoryInterpreter
import infrastructure.repository.slick.SlickFooRepositoryInterpreter

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import eu.timepit.refined.pureconfig._

object Main extends IOApp {

  private lazy val conf: AppConfig = ConfigSource
    .resources("config/app.conf")
    .loadOrThrow[AppConfig]

  def dbRes[F[_]: Sync]: Resource[F, DatabaseDef] = {
    val release = (db: DatabaseDef) => Sync[F].delay(db.close())
    val acquire = Sync[F].delay(Database.forDriver(
      new org.postgresql.Driver,
      conf.db.url.value,
      conf.db.user.value,
      conf.db.password
    ))

    Resource.make(acquire)(release)
  }

  private def makeAppStream[F[+_]: Async]: Resource[F, Stream[F, Unit]] = {
    for {
      db <- dbRes[F]
      fooRepositoryAlg = new SlickFooRepositoryInterpreter[F](db)
      fooValidationAlg = new FooValidationInterpreter[F](fooRepositoryAlg)
      userRepositoryAlg = new MemoryUserRepositoryInterpreter[F]
      jwtClockAlg = JwtClockAlg[F]
      jwtTokenAlg = new JwtTokenInterpreter[F](conf.jwt, jwtClockAlg)
      hasherAlg = new BCryptHasherInterpreter[F](12)
      fooService = new FooService[F](fooRepositoryAlg, fooValidationAlg)
      authService = new AuthService[F](jwtTokenAlg, userRepositoryAlg, hasherAlg)
      deleteFooConsumer = new DeleteFooConsumer[F](fooService, conf)
      deleteFooProducer = new DeleteFooProducer[F](conf)

      routes = makeRoutes[F](List(
        new CreateFooEndpoint[F](fooService, jwtTokenAlg).action,
        new GetFooEndpoint[F](fooService, jwtTokenAlg).action,
        new DeleteFooEndpoint[F](deleteFooProducer, jwtTokenAlg).action,
        new CreateJwtTokenEndpoint[F](authService).action,
      ))
    } yield {
      makeServerStream(routes)
        .merge(deleteFooConsumer.stream)
    }
  }

  private def makeRoutes[F[_]: Async](
    apiEndpoints: List[ServerEndpoint[Any, F]]
  ): HttpRoutes[F] = {
    val swaggerEndpoints = SwaggerInterpreter().fromServerEndpoints[F](
      endpoints = apiEndpoints,
      title = "Learning tapir",
      version = "1.0.0"
    )

    Http4sServerInterpreter[F].toRoutes(apiEndpoints) <+>
    Http4sServerInterpreter[F].toRoutes(swaggerEndpoints) // docs
  }

  private def makeServerStream[F[_]: Async](routes: HttpRoutes[F]): Stream[F, Unit] = {

    val httpApp = Logger.httpApp[F](
      logHeaders = false,
      logBody = false
    )(routes.orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(
        port = conf.server.port.value,
        host = conf.server.host.value
      )
      .withHttpApp(httpApp)
      .serve
      .void
  }

  override def run(args: List[String]): IO[ExitCode] = {
    makeAppStream[IO].use(_.compile.drain.as(ExitCode.Success))
  }
}
