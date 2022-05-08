package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.{JwtToken, JwtTokenAlg}
import domain.foos.FooId
import domain.users.UserId
import infrastructure.endpoint.{ApiEndpoint, ErrorInfo}
import infrastructure.messaging.kafka.DeleteFooProducer
import util.tapir.securedEndpoint

import cats.effect.kernel.Async
import cats.implicits._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint.Full

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  jwtTokenAlg: JwtTokenAlg[F],
) extends ApiEndpoint {

  val action: Full[JwtToken, UserId, FooId, ErrorInfo, Unit, Any, F] =
    securedEndpoint(jwtTokenAlg)
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foo" / path[FooId]("fooId"))
      .serverLogic { _ => foo =>
        producer
          .produce(foo)
          .map(_ => ().asRight)
      }
}
