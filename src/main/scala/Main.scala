package org.whsv26.tapir

import application.endpoint.{foos, jwt}
import application.security.ServerEndpoints
import config.Config.{AppConfig, ServerConfig}
import domain.auth.{AuthService, JwtClockAlg}
import domain.foos.{FooService, FooValidationAlgInterpreter}
import infrastructure.auth.{BCryptHasherAlgInterpreter, JwtTokenAlgInterpreter}
import infrastructure.messaging.kafka.{DeleteFooConsumer, DeleteFooProducer}
import infrastructure.storage.inmemory.MemUserRepositoryAlgInterpreter
import infrastructure.storage.slick.{SlickDatabaseFactory, SlickFooRepositoryAlgInterpreter}

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {
  private def makeAppStream[F[_]: Async]: Resource[F, Stream[F, Unit]] =
    for {
      conf <- AppConfig("config/app.conf")
      db <- SlickDatabaseFactory(conf.db)
      jwtClockAlg <- JwtClockAlg[F]
      userRepositoryAlg <- MemUserRepositoryAlgInterpreter[F]
      hasherAlg <- BCryptHasherAlgInterpreter(12)
      fooRepositoryAlg <- SlickFooRepositoryAlgInterpreter(db)
      fooValidationAlg <- FooValidationAlgInterpreter(fooRepositoryAlg)
      jwtTokenAlg <- JwtTokenAlgInterpreter(conf.jwt, jwtClockAlg)
      fooService <- FooService(fooRepositoryAlg, fooValidationAlg)
      authService <- AuthService(jwtTokenAlg, userRepositoryAlg, hasherAlg)
      deleteFooConsumer <- DeleteFooConsumer(fooService, conf)
      deleteFooProducer <- DeleteFooProducer(conf)

      routes = makeRoutes[F](List(
        foos.serverEndpoints(fooService, jwtTokenAlg, deleteFooProducer),
        jwt.serverEndpoints(authService),
      ).flatten)

    } yield makeServerStream(conf.server, routes).merge(deleteFooConsumer.stream)

  private def makeRoutes[F[_]: Async](
    serverEndpoints: ServerEndpoints[F]
  ): HttpRoutes[F] = {
    val swaggerUiEndpoints =
      SwaggerInterpreter()
        .fromServerEndpoints[F](
          endpoints = serverEndpoints,
          title = "Learning tapir",
          version = "1.0.0"
        )

    Http4sServerInterpreter[F].toRoutes(serverEndpoints) <+>
    Http4sServerInterpreter[F].toRoutes(swaggerUiEndpoints)
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
      .bindHttp(conf.port.value, conf.host.value)
      .withHttpApp(httpApp)
      .serve
      .void
  }

  override def run(args: List[String]): IO[ExitCode] =
    makeAppStream[IO]
      .use(_.compile.drain)
      .as(ExitCode.Success)
}
