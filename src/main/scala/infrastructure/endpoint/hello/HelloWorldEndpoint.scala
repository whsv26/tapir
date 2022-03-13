package org.whsv26.tapir
package infrastructure.endpoint.hello

import infrastructure.endpoint.ApiEndpoint

import cats.Applicative
import cats.implicits._
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class HelloWorldEndpoint[F[_]: Applicative] extends ApiEndpoint {
  val action: Full[Unit, Unit, String, Unit, String, Any, F] =
    endpoint
      .in(prefix / "hello-world")
      .get
      .in(query[String]("whom"))
      .out(jsonBody[String])
      .serverLogic[F] { whom =>
        s"Hello world and $whom!"
          .asRight[Unit]
          .pure[F]
      }
}
