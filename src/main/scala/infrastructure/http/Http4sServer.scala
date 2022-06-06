package org.whsv26.tapir
package infrastructure.http

import config.Config.ServerConfig

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.syntax.functor._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object Http4sServer {
  def start[F[_]: Async](
    conf: ServerConfig,
    routes: HttpRoutes[F]
  ): Resource[F, Unit] = {

    val httpApp = Logger.httpApp[F](
      logHeaders = false,
      logBody = false
    )(routes.orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(conf.port.value, conf.host.value)
      .withHttpApp(httpApp)
      .resource
      .void
  }
}
