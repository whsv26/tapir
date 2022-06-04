package org.whsv26.tapir
package application.endpoint.foos

import application.endpoint.foos.GetFooEndpoint.NotFoundApiError
import application.error.{ApiError, EntityNotFound}
import application.security.{SecuredRoute, securedEndpoint, tokenAuth}
import domain.auth.{Token, TokenAlg}
import domain.foos.{Foo, FooId, FooService}

import cats.effect.kernel.Sync
import cats.syntax.functor._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredRoute[F, FooId, Foo] =
    GetFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => fooId =>
        fooService
          .findById(fooId)
          .map(_.toRight(NotFoundApiError(fooId.value.toString)))
      }
}

object GetFooEndpoint {
  lazy val route: Endpoint[Token, FooId, ApiError, Foo, Any] =
    securedEndpoint
      .summary("Get foo info")
      .get
      .in("api" / "v1" / "foo")
      .in(path[FooId])
      .out(jsonBody[Foo])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private object NotFoundApiError extends EntityNotFound("Foo")
}