package org.whsv26.tapir

import auth._
import config.Config.{AppConfig, DbConfig}
import foos.SlickFooRepositoryAlgInterpreter
import foos.delete.{DeleteFooConsumer, DeleteFooProducer}
import util.http.Http4sServer
import util.http.security.ServerEndpoints
import util.slick.SlickDatabaseFactory

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    application[IO].use(_ => IO.never)

  private def application[F[_]: Async]: Resource[F, Unit] =
    for {
      conf              <- AppConfig.read("config/app.conf")
      db                <- SlickDatabaseFactory(conf.db)
      transactor        <- mkTransactor(conf.db)
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

  private def mkTransactor[F[_]: Async](conf: DbConfig): Resource[F, Transactor[F]] =
    ExecutionContexts
      .fixedThreadPool(100)
      .flatMap { connectionExecutionContext =>
        HikariTransactor.newHikariTransactor[F](
          driverClassName = "org.postgresql.Driver",
          url = conf.url.value,
          user = conf.user.value,
          pass = conf.password,
          connectEC = connectionExecutionContext
        )
      }
}
