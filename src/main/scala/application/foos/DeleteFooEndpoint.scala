package org.whsv26.tapir
package application.foos

import application.{SecuredRoute, securedEndpoint}
import domain.auth.TokenAlg
import domain.foos.FooId
import infrastructure.messaging.kafka.DeleteFooProducer

import cats.effect.kernel.Async
import cats.implicits._
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: TokenAlg[F],
) {

  val route: SecuredRoute[F, FooId, Unit] =
    securedEndpoint(tokens)
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foo" / path[FooId]("fooId"))
      .serverLogic { _ => foo =>
        producer
          .produce(foo)
          .map(_ => ().asRight)
      }
}