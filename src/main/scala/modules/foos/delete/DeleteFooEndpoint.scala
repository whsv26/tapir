package org.whsv26.tapir
package modules.foos.delete

import modules.auth.Tokens
import modules.foos.Foo
import modules.foos.Foo.Id
import util.http.security._

import cats.effect.kernel.Async
import cats.implicits._
import sttp.model.StatusCode
import sttp.tapir._

class DeleteFooEndpoint[F[_]: Async](
  producer: DeleteFooProducer[F],
  tokens: Tokens[F]
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
  lazy val route: SecuredRoute[Id, Unit] =
    securedEndpoint
      .summary("Delete foo")
      .delete
      .in("foos" / path[Foo.Id]("ID"))
      .out(statusCode(StatusCode.Accepted))
}
