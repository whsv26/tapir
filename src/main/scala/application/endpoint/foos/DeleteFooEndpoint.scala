package org.whsv26.tapir
package application.endpoint.foos

import application.security.{SecuredRoute, SecuredServerRoute, securedEndpoint, tokenAuth}
import domain.auth.TokenAlg
import domain.foos.FooId
import infrastructure.messaging.kafka.DeleteFooProducer

import cats.effect.kernel.Async
import cats.implicits._
import sttp.model.StatusCode
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, FooId, Unit] =
    DeleteFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => foo =>
        producer
          .produce(foo)
          .map(_ => ().asRight)
      }
}

object DeleteFooEndpoint {
  lazy val route: SecuredRoute[FooId, Unit] =
    securedEndpoint
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foos" / path[FooId]("fooId"))
      .out(statusCode(StatusCode.Accepted))
}
