package org.whsv26.tapir
package foos.delete

import auth.TokenAlg
import foos.Foo
import util.http.security._

import cats.effect.kernel.Async
import cats.implicits._
import sttp.model.StatusCode
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, Foo.Id, Unit] =
    DeleteFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => id =>
        producer
          .produce(id)
          .as(().asRight)
      }
}

object DeleteFooEndpoint {
  lazy val route: SecuredRoute[Foo.Id, Unit] =
    securedEndpoint
      .summary("Delete foo")
      .delete
      .in("api" / "v1" / "foos" / path[Foo.Id]("ID"))
      .out(statusCode(StatusCode.Accepted))
}
