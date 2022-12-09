package org.whsv26.tapir
package foos.update

import foos.FooValidation.FooDoesNotExist
import foos.update.UpdateFooEndpoint.{NotFoundApiError, UpdateFoo, UpdatedFoo}
import foos.{Foo, FooService}
import util.http.error.{ApiError, EntityNotFound}
import util.http.security._

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import org.whsv26.tapir.auth.TokenAlg
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class UpdateFooEndpoint[F[_]: Sync](
  foos: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, (Foo.Id, UpdateFoo), UpdatedFoo] =
    UpdateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => { case (id, cmd) =>
        foos.update(Foo(id, cmd.a, cmd.b))
          .map(_.transformInto[UpdatedFoo])
          .leftMap { case FooDoesNotExist(id) => NotFoundApiError(id.value.toString) }
          .value
      }}
}

object UpdateFooEndpoint {
  lazy val route: SecuredRoute[(Foo.Id, UpdateFoo), UpdatedFoo] =
    securedEndpoint
      .summary("Update existing foo")
      .patch
      .in("api" / "v1" / "foos")
      .in(path[Foo.Id]("Foo.Id"))
      .in(jsonBody[UpdateFoo])
      .out(jsonBody[UpdatedFoo])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private[foos] case class UpdateFoo(a: NonNegInt, b: Boolean)

  private[foos] case class UpdatedFoo(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}
