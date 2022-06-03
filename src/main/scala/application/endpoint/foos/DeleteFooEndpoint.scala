package org.whsv26.tapir
package application.endpoint.foos

import application.error.ApiError
import application.security.{SecuredRoute, securedEndpoint, tokenAuth}
import domain.auth.{Token, TokenAlg}
import domain.foos.FooId
import infrastructure.messaging.kafka.DeleteFooProducer

import cats.effect.kernel.Async
import cats.implicits._
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: TokenAlg[F]
) {
  val route: SecuredRoute[F, FooId, Unit] =
    DeleteFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => foo =>
        producer
          .produce(foo)
          .map(_ => ().asRight)
      }
}

object DeleteFooEndpoint {
  val route: Endpoint[Token, FooId, ApiError, Unit, Any] =
    securedEndpoint
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foo" / path[FooId]("fooId"))
}
