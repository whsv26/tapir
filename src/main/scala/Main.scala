package org.whsv26.tapir

import application.endpoint.{foos, jwt}
import application.security.ServerEndpoints
import config.Config.AppConfig
import domain.auth.{AuthService, JwtClockAlg}
import domain.foos.{FooService, FooValidationAlgInterpreter}
import infrastructure.auth.{BCryptHasherAlgInterpreter, JwtTokenAlgInterpreter}
import infrastructure.http.Http4sServer
import infrastructure.messaging.kafka.{DeleteFooConsumer, DeleteFooProducer}
import infrastructure.storage.inmemory.MemUserRepositoryAlgInterpreter
import infrastructure.storage.slick.{SlickDatabaseFactory, SlickFooRepositoryAlgInterpreter}

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    application[IO].use(_ => IO(ExitCode.Success))

  private def application[F[_]: Async]: Resource[F, Unit] =
    for {
      conf              <- AppConfig.read("config/app.conf")
      db                <- SlickDatabaseFactory(conf.db)
      jwtClockAlg       <- JwtClockAlg[F]
      userRepositoryAlg <- MemUserRepositoryAlgInterpreter[F]
      hasherAlg         <- BCryptHasherAlgInterpreter(12)
      fooRepositoryAlg  <- SlickFooRepositoryAlgInterpreter(db)
      fooValidationAlg  <- FooValidationAlgInterpreter(fooRepositoryAlg)
      jwtTokenAlg       <- JwtTokenAlgInterpreter(conf.jwt, jwtClockAlg)
      fooService        <- FooService(fooRepositoryAlg, fooValidationAlg)
      authService       <- AuthService(jwtTokenAlg, userRepositoryAlg, hasherAlg)
      deleteFooProducer <- DeleteFooProducer(conf)

      routes = http4sRoutes[F](List(
        foos.serverEndpoints(fooService, jwtTokenAlg, deleteFooProducer),
        jwt.serverEndpoints(authService),
      ).flatten)

      _ <- DeleteFooConsumer.start(fooService, conf)
      _ <- Http4sServer.start(conf.server, routes)

    } yield ()

  private def http4sRoutes[F[_]: Async](
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
}
