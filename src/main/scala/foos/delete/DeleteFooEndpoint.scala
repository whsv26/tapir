package org.whsv26.tapir
package foos.delete

import cats.effect.kernel.Async
import cats.implicits._
import org.whsv26.tapir.auth.TokenAlg
import org.whsv26.tapir.foos.Foo
import org.whsv26.tapir.util.http.security._
import sttp.model.StatusCode
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, Foo.Id, Unit] =
    DeleteFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => foo =>
        producer
          .produce(foo)
          .map(_ => ().asRight)
      }
}

object DeleteFooEndpoint {
  lazy val route: SecuredRoute[Foo.Id, Unit] =
    securedEndpoint
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foos" / path[Foo.Id]("Foo.Id"))
      .out(statusCode(StatusCode.Accepted))
}
