package org.whsv26.tapir

import application.endpoint.{foos, jwt}
import config.Config.{AppConfig, ServerConfig}
import domain.auth.{AuthService, JwtClockAlg}
import domain.foos.{FooService, FooValidationInterpreter}
import infrastructure.auth.{BCryptHasherAlgInterpreter, JwtTokenAlgInterpreter}
import infrastructure.messaging.kafka.{DeleteFooConsumer, DeleteFooProducer}
import infrastructure.repository.inmemory.MemUserRepositoryAlgInterpreter
import infrastructure.repository.slick.SlickFooRepositoryAlgInterpreter

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import eu.timepit.refined.pureconfig._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {

  private case class ConfigError(err: ConfigReaderFailures) extends Throwable {
    override val getMessage = err.prettyPrint()
  }

  private def confResource[F[_]: Sync]: Resource[F, AppConfig] =
    Resource.eval {
      Sync[F].delay(ConfigSource.resources("config/app.conf"))
        .map(_.load[AppConfig].leftMap(ConfigError))
        .rethrow
    }

  private def dbResource[F[_]: Sync](conf: AppConfig): Resource[F, DatabaseDef] =
    Resource.fromAutoCloseable(Sync[F].delay(Database.forDriver(
      new org.postgresql.Driver,
      conf.db.url.value,
      conf.db.user.value,
      conf.db.password
    )))

  private def makeAppStream[F[_]: Async]: Resource[F, Stream[F, Unit]] =
    for {
      conf <- confResource[F]
      db <- dbResource(conf)
      jwtClockAlg <- JwtClockAlg[F]
      userRepositoryAlg <- MemUserRepositoryAlgInterpreter[F]
      hasherAlg <- BCryptHasherAlgInterpreter(12)
      fooRepositoryAlg <- SlickFooRepositoryAlgInterpreter(db)
      fooValidationAlg <- FooValidationInterpreter(fooRepositoryAlg)
      jwtTokenAlg <- JwtTokenAlgInterpreter(conf.jwt, jwtClockAlg)
      fooService <- FooService(fooRepositoryAlg, fooValidationAlg)
      authService <- AuthService(jwtTokenAlg, userRepositoryAlg, hasherAlg)
      deleteFooConsumer <- DeleteFooConsumer(fooService, conf)
      deleteFooProducer <- DeleteFooProducer(conf)

      routes = makeRoutes[F](List(
        foos.routes(fooService, jwtTokenAlg, deleteFooProducer),
        jwt.routes(authService),
      ).flatten)

    } yield makeServerStream(conf.server, routes).merge(deleteFooConsumer.stream)

  private def makeRoutes[F[_]: Async](
    serverEndpoints: List[ServerEndpoint[Any, F]]
  ): HttpRoutes[F] = {
    val swaggerUiEndpoints =
      SwaggerInterpreter()
        .fromServerEndpoints[F](
          endpoints = serverEndpoints,
          title = "Learning tapir",
          version = "1.0.0"
        )

    Http4sServerInterpreter[F].toRoutes(serverEndpoints) <+>
    Http4sServerInterpreter[F].toRoutes(swaggerUiEndpoints) // docs
  }

  private def makeServerStream[F[_]: Async](
    conf: ServerConfig,
    routes: HttpRoutes[F]
  ): Stream[F, Unit] = {

    val httpApp = Logger.httpApp[F](
      logHeaders = false,
      logBody = false
    )(routes.orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(
        port = conf.port.value,
        host = conf.host.value
      )
      .withHttpApp(httpApp)
      .serve
      .void
  }

  override def run(args: List[String]): IO[ExitCode] =
    makeAppStream[IO]
      .use(_.compile.drain)
      .as(ExitCode.Success)
}
