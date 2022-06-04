package org.whsv26.tapir

import config.Config.AppConfig
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
import org.whsv26.tapir.application.endpoint.{foos, jwt}
import org.whsv26.tapir.application.endpoint.foos.{CreateFooEndpoint, DeleteFooEndpoint, GetFooEndpoint}
import org.whsv26.tapir.application.endpoint.jwt.CreateJwtTokenEndpoint
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.openapi.{Info, OpenAPI}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {

  private lazy val conf: AppConfig = ConfigSource
    .resources("config/app.conf")
    .loadOrThrow[AppConfig]

  def dbResource[F[_]: Sync]: Resource[F, DatabaseDef] =
    Resource.fromAutoCloseable(Sync[F].delay(Database.forDriver(
      new org.postgresql.Driver,
      conf.db.url.value,
      conf.db.user.value,
      conf.db.password
    )))

  private def makeAppStream[F[+_]: Async]: Resource[F, Stream[F, Unit]] = {
    for {
      db <- dbResource[F]
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
    } yield {
      makeServerStream(routes)
        .merge(deleteFooConsumer.stream)
    }
  }

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
