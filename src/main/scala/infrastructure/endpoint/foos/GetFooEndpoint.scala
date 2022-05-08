package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.TokenAlg
import domain.foos.{Foo, FooId, FooService}
import infrastructure.endpoint.foos.GetFooEndpoint.NotFoundApiError
import infrastructure.endpoint.{ApiError, EntityNotFound}
import util.tapir.{SecuredRoute, securedEndpoint}

import cats.effect.kernel.Sync
import cats.syntax.functor._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  tokens: TokenAlg[F],
) {
  val route: SecuredRoute[F, FooId, Foo] =
    securedEndpoint(tokens)
      .summary("Get foo info")
      .get
      .in("api" / "v1" / "foo")
      .in(path[FooId])
      .out(jsonBody[Foo])
      .errorOutVariant(oneOfVariant(
        statusCode
          .description(NotFoundApiError.status, NotFoundApiError.format)
          .and(stringBody)
          .mapTo[ApiError]
      ))
      .serverLogic { _ => fooId =>
        fooService
          .findById(fooId)
          .map(_.toRight(NotFoundApiError(fooId.value.toString)))
      }
}

object GetFooEndpoint {
  private object NotFoundApiError extends EntityNotFound("Foo")
}