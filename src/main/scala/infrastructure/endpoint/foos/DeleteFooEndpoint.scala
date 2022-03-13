package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.foos.Foo.FooId
import infrastructure.endpoint.ApiEndpoint
import infrastructure.messaging.kafka.DeleteFooProducer

import cats.effect.kernel.Async
import cats.implicits._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint.Full

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F]
) extends ApiEndpoint {

  private val action = endpoint
    .in(prefix / "foo" / path[FooId]("fooId") )
    .delete
    .serverLogic[F] { foo =>
      producer
        .produce(foo)
        .map(_ => ().asRight[Unit])
    }
}

object DeleteFooEndpoint {
  def apply[F[_]: Async](
    producer: DeleteFooProducer[F]
  ): Full[Unit, Unit, FooId, Unit, Unit, Any, F] = {
    new DeleteFooEndpoint[F](producer).action
  }
}
