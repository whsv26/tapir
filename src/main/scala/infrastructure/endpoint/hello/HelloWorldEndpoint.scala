package org.whsv26.tapir
package infrastructure.endpoint.hello

import cats.Applicative
import cats.implicits._
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class HelloWorldEndpoint[F[_]: Applicative] {
  private val helloWorld = endpoint
    .in("api" / "v1" / "hello-world")
    .get
    .in(query[String]("whom"))
    .out(jsonBody[String])
    .serverLogic[F] { whom =>
      s"Hello world and $whom!"
        .asRight[Unit]
        .pure[F]
    }
}

object HelloWorldEndpoint {
  def apply[F[_] : Applicative]: Full[Unit, Unit, String, Unit, String, Any, F] =
    new HelloWorldEndpoint[F].helloWorld
}
