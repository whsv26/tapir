package org.whsv26.tapir

import config.Config.AppConfig
import util.http.Http4sServer

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.HttpRoutes
import org.whsv26.tapir.auth.{AuthService, BCryptHasherAlgInterpreter, JwtClockAlg, JwtTokenAlgInterpreter, MemUserRepositoryAlgInterpreter}
import org.whsv26.tapir.foos.{FooService, FooValidationAlgInterpreter, SlickFooRepositoryAlgInterpreter}
import org.whsv26.tapir.foos.delete.{DeleteFooConsumer, DeleteFooProducer}
import org.whsv26.tapir.util.http.security.ServerEndpoints
import org.whsv26.tapir.util.slick.SlickDatabaseFactory
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    application[IO].use(_ => IO.never)

  private def application[F[_]: Async]: Resource[F, Unit] =
    for {
      conf              <- AppConfig.read("config/app.conf")
      db                <- SlickDatabaseFactory(conf.db)
      jwtClockAlg       <- JwtClockAlg[F]
      userRepositoryAlg <- MemUserRepositoryAlgInterpreter[F]
      hasherAlg         <- BCryptHasherAlgInterpreter(12)
      fooRepositoryAlg  <- SlickFooRepositoryAlgInterpreter(db)
      fooValidationAlg  <- foos.FooValidationAlgInterpreter(fooRepositoryAlg)
      jwtTokenAlg       <- JwtTokenAlgInterpreter(conf.jwt, jwtClockAlg)
      fooService        <- foos.FooService(fooRepositoryAlg, fooValidationAlg)
      authService       <- AuthService(jwtTokenAlg, userRepositoryAlg, hasherAlg)
      deleteFooProducer <- DeleteFooProducer(conf)

      routes = http4sRoutes[F](List(
        foos.serverEndpoints(fooService, jwtTokenAlg, deleteFooProducer),
        auth.serverEndpoints(authService),
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
