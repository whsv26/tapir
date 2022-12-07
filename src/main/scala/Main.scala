package org.whsv26.tapir

import auth.{AuthService, _}
import config.Config.{AppConfig, DbConfig}
import foos.{FooService, FooValidationAlgInterpreter, SlickFooRepositoryAlgInterpreter}
import foos.delete.{DeleteFooConsumer, DeleteFooProducer}
import util.bus.{Mediator, NotificationHandlerBase, RequestHandlerBase}
import util.http.Http4sServer
import util.http.security.ServerEndpoints
import util.slick.SlickDatabaseFactory

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import com.softwaremill.macwire.autocats.autowire
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.whsv26.tapir.foos.create.CreateHandler
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import com.softwaremill.tagging._
import org.whsv26.tapir.auth.BCryptHasherAlgInterpreter.RoundsTag

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    application.useForever

  case class Handlers(
    createFooHandler: foos.create.CreateHandler[IO]
  )

  case class Deps(
    config: AppConfig,
    jwtClock: JwtClockAlg.SystemClock[IO],
    tokenAlg: JwtTokenAlgInterpreter[IO],
    slickDatabaseFactory: slick.jdbc.JdbcBackend.DatabaseDef,
    transactor: HikariTransactor[IO],
    handlers: Handlers,
    mediator: Mediator[IO],
    userRepository: MemUserRepositoryAlgInterpreter[IO],
    hasher: BCryptHasherAlgInterpreter[IO],
    fooRepository: SlickFooRepositoryAlgInterpreter[IO],
    fooValidationAlg: FooValidationAlgInterpreter[IO],
    fooService: FooService[IO],
    deleteFooProducer: DeleteFooProducer[IO],
    authService: AuthService[IO],
  )

  val config = AppConfig.read[IO]("config/app.conf")

  private def application: Resource[IO, Unit] = {
    autowire[Deps](
      AppConfig.read[IO]("config/app.conf"),
      config.map(_.db),
      config.map(_.jwt),
      SlickDatabaseFactory[IO] _,
      mkHikariTransactor _,
      mkMediator _,
      12.taggedWith[RoundsTag]
    ).flatMap { deps =>

      val routes = http4sRoutes[IO](List(
        foos.serverEndpoints(deps.fooService, deps.tokenAlg, deps.deleteFooProducer, deps.mediator),
        auth.serverEndpoints(deps.authService),
      ).flatten)

      for {
        _ <- DeleteFooConsumer.start(deps.fooService, deps.config)
        _ <- Http4sServer.start(deps.config.server, routes)
      } yield ()
    }
  }

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

  private def mkHikariTransactor(conf: DbConfig): Resource[IO, HikariTransactor[IO]] =
    mkTransactor[IO](conf)

  private def mkTransactor[F[_]: Async](conf: DbConfig): Resource[F, HikariTransactor[F]] =
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

  private def mkMediator(handlers: Handlers): Mediator[IO] = {
    val genericHandlers = shapeless.Generic[Handlers].to(handlers)

    val requestHandlers = genericHandlers
      .unifySubtypes[RequestHandlerBase[IO]]
      .filter[RequestHandlerBase[IO]]
      .to[List]

    val notificationHandlers = genericHandlers
      .unifySubtypes[NotificationHandlerBase[IO]]
      .filter[NotificationHandlerBase[IO]]
      .to[List]

    new Mediator.Impl(requestHandlers, notificationHandlers)
  }
}
