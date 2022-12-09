package org.whsv26.tapir
package util.http

import config.Config.ServerConfig
import util.http.security.ServerEndpoints

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.Logger
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Http4sServerFactory {
  def apply[F[_]: Async](
    conf: ServerConfig,
    endpoints: Set[ServerEndpoint[Any, F]]
  ): Resource[F, Server] = {
    val routes = http4sRoutes[F](endpoints.toList)

    val httpApp = Logger.httpApp[F](
      logHeaders = false,
      logBody = false
    )(routes.orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(conf.port.value, conf.host.value)
      .withHttpApp(httpApp)
      .resource
  }

  private def http4sRoutes[F[_] : Async](
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
