package org.whsv26.tapir

import config.Config.{AppConfig, DbConfig, JwtConfig, ServerConfig}
import modules.foos.delete.DeleteFooConsumer
import modules.{auth, foos}
import util.bus.Mediator
import util.doobie.DoobieTransactorFactory
import util.http.Http4sServerFactory
import util.slick.SlickDatabaseDefFactory

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Async
import distage._
import doobie.util.transactor.Transactor
import izumi.distage.model.definition.ModuleDef
import org.http4s.server.Server
import org.whsv26.tapir.util.time.Clock
import slick.jdbc.JdbcBackend.DatabaseDef

object Main extends IOApp {
  case class Application(
    deleteFooConsumer: DeleteFooConsumer[IO],
  )

  override def run(args: List[String]): IO[ExitCode] =
    application.use { app =>
      for {
        _ <- app.deleteFooConsumer.start
        _ <- IO.never[Nothing]
      } yield ExitCode.Success
    }

  private def application: Lifecycle[IO, Application] = {
    val injector = Injector[IO]()
    val plan = injector.plan(appModule[IO], Activation.empty, Roots.target[Application])

    injector
      .produce(plan)
      .map(_.get[Application])
  }

  private def appModule[F[_]: Async: TagK] = new ModuleDef {
    include(auth.module[F])
    include(foos.module[F])

    make[AppConfig].fromEffect(AppConfig.read[F]("config/app.conf"))
    make[Clock[F]].from[Clock.SystemImpl[F]]
    make[DbConfig].from((conf: AppConfig) => conf.db)
    make[JwtConfig].from((conf: AppConfig) => conf.jwt)
    make[ServerConfig].from((conf: AppConfig) => conf.server)
    make[Mediator[F]].from[Mediator.Impl[F]]
    make[DatabaseDef].fromResource(SlickDatabaseDefFactory[F] _)
    make[Transactor[F]].fromResource(DoobieTransactorFactory[F] _)
    make[Server].fromResource(Http4sServerFactory[F] _)
    make[Application]
  }
}
