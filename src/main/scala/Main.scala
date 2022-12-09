package org.whsv26.tapir

import auth._
import config.Config.{AppConfig, DbConfig, JwtConfig}
import foos.create.CreateFooHandler
import foos.delete.{DeleteFooConsumer, DeleteFooHandler, DeleteFooProducer}
import foos.{FooRepository, FooService, FooValidation, foosModule}
import util.bus.{Mediator, NotificationHandlerBase, RequestHandlerBase}
import util.http.Http4sServer
import util.http.security.ServerEndpoints

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import distage.{Activation, Injector, Lifecycle, Roots, Tag, TagK}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import slick.jdbc.JdbcBackend
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import izumi.distage.model.definition.ModuleDef
import org.whsv26.tapir.util.slick.SlickDatabaseFactory

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    application.use(_ => IO.never)

  case class Handlers(
    createFooHandler: CreateFooHandler[IO],
    deleteFooHandler: DeleteFooHandler[IO],
  )

  case class Deps(
    config: AppConfig,
    jwtClock: JwtClockAlg.SystemClockImpl[IO],
    jwtToken: JwtTokenAlgInterpreter[IO],
    slickDatabaseFactory: JdbcBackend.DatabaseDef,
    transactor: HikariTransactor[IO],
    handlers: Handlers,
    mediator: Mediator[IO],
    userRepository: MemUserRepositoryAlgInterpreter[IO],
    hasher: BCryptHasherAlgInterpreter[IO],
    fooRepository: FooRepository.SlickImpl[IO],
    fooValidation: FooValidation.Impl[IO],
    fooService: FooService[IO],
    deleteFooProducer: DeleteFooProducer[IO],
    authService: AuthService[IO],
  )

  def appModule[F[_]: Async: TagK] = new ModuleDef {
    include(authModule[F])
    include(foosModule[F])

    make[AppConfig].fromEffect(AppConfig.read[F]("config/app.conf"))
    make[DbConfig].from((conf: AppConfig) => conf.db)
    make[JwtConfig].from((conf: AppConfig) => conf.jwt)

    make[JdbcBackend.DatabaseDef].fromResource(SlickDatabaseFactory[IO] _)
    make[Transactor[F]].fromResource(mkTransactor[F] _)
    make[Mediator[F]].from[Mediator.Impl[F]]

    make[Handlers]
    make[Deps]
  }

  private def application: Lifecycle[IO, Unit] = {
    val injector: Injector[IO] = Injector[IO]()

    val plan = injector.plan(
      appModule[IO],
      Activation.empty,
      Roots.target[Deps]
    )

    injector
      .produce(plan)
      .map(_.get[Deps])
      .flatMap { deps =>
        val routes = http4sRoutes[IO](List(
          foos.serverEndpoints(deps.fooService, deps.jwtToken, deps.mediator, deps.deleteFooProducer),
          auth.serverEndpoints(deps.authService),
        ).flatten)

        for {
          _ <- Lifecycle.fromCats(DeleteFooConsumer.start(deps.fooService, deps.config))
          _ <- Lifecycle.fromCats(Http4sServer.start(deps.config.server, routes))
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

  private def mkTransactor[F[_]: Async](conf: DbConfig): Resource[F, Transactor[F]] =
    ExecutionContexts
      .fixedThreadPool[F](100)
      .flatMap { connectionExecutionContext =>
        HikariTransactor.newHikariTransactor[F](
          driverClassName = "org.postgresql.Driver",
          url = conf.url.value,
          user = conf.user.value,
          pass = conf.password,
          connectEC = connectionExecutionContext
        )
      }

  private def mkMediator[F[_]: Async](handlers: Handlers): Mediator[F] = {
    val genericHandlers = shapeless.Generic[Handlers].to(handlers)

    val requestHandlers = genericHandlers
      .unifySubtypes[RequestHandlerBase[F]]
      .filter[RequestHandlerBase[F]]
      .to[List]

    val notificationHandlers = genericHandlers
      .unifySubtypes[NotificationHandlerBase[F]]
      .filter[NotificationHandlerBase[F]]
      .to[List]

    new Mediator.Impl(requestHandlers, notificationHandlers)
  }
}
