package org.whsv26.tapir
package application.endpoint.foos

import application.endpoint.foos.UpdateFooEndpoint.{NotFoundApiError, UpdateFoo, UpdatedFoo}
import application.error.{ApiError, EntityNotFound}
import application.security.{SecuredRoute, SecuredServerRoute, securedEndpoint, tokenAuth}
import domain.auth.TokenAlg
import domain.foos.FooValidationAlg.FooDoesNotExist
import domain.foos.{FooId, FooService}

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class UpdateFooEndpoint[F[_]: Sync](
  foos: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, (FooId, UpdateFoo), UpdatedFoo] =
    UpdateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => { case (id, cmd) =>
        foos.update(id, cmd)
          .map(foo => UpdatedFoo(foo.id, foo.a, foo.b))
          .leftMap { case FooDoesNotExist(id) => NotFoundApiError(id.value.toString) }
          .value
      }}
}

object UpdateFooEndpoint {
  lazy val route: SecuredRoute[(FooId, UpdateFoo), UpdatedFoo] =
    securedEndpoint
      .summary("Update existing foo")
      .patch
      .in("api" / "v1" / "foos")
      .in(path[FooId]("fooId"))
      .in(jsonBody[UpdateFoo])
      .out(jsonBody[UpdatedFoo])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  final case class UpdateFoo(a: NonNegInt, b: Boolean)
  final case class UpdatedFoo(id: FooId, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}
