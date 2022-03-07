package org.whsv26.tapir

import Foo.FooId
import cats.effect.kernel.Async
import cats.implicits._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint.Full

class DeleteFooEndpoint[F[_]: Async](producer: DeleteFooProducer[F]) {

  private val deleteFoo = endpoint
    .in("api" / "v1" / "foo" / path[FooId] )
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
    new DeleteFooEndpoint[F](producer).deleteFoo
  }
}
