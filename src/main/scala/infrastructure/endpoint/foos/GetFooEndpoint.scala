package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.{Token, TokenAlg}
import domain.foos.{Foo, FooId, FooService}
import domain.users.UserId
import infrastructure.endpoint.ErrorInfo
import infrastructure.endpoint.ErrorInfo.Foo.NotFound
import util.tapir.securedEndpoint

import cats.effect.kernel.Sync
import cats.syntax.functor._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  tokens: TokenAlg[F],
) {
  val action: Full[Token, UserId, FooId, ErrorInfo, Foo, Any, F] =
    securedEndpoint(tokens)
      .summary("Get foo info")
      .get
      .in("api" / "v1" / "foo")
      .in(path[FooId])
      .out(jsonBody[Foo])
      .errorOutVariant(oneOfVariant(
        statusCode
          .description(NotFound.status, NotFound.format)
          .and(stringBody)
          .mapTo[ErrorInfo]
      ))
      .serverLogic { _ => fooId =>
        fooService
          .findById(fooId)
          .map(_.toRight(NotFound(fooId.value.toString)))
      }
}