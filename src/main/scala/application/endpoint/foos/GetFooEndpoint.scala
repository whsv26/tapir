package org.whsv26.tapir
package application.endpoint.foos

import application.endpoint.foos.GetFooEndpoint.{FooView, NotFoundApiError}
import application.error.{ApiError, EntityNotFound}
import application.security.{SecuredRoute, SecuredServerRoute, securedEndpoint, tokenAuth}
import domain.auth.TokenAlg
import domain.foos.{FooId, FooService}

import cats.data.OptionT
import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, FooId, FooView] =
    GetFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => fooId =>
        OptionT(fooService.findById(fooId))
          .toRight(NotFoundApiError(fooId.value.toString))
          .map(_.transformInto[FooView])
          .value
      }
}

object GetFooEndpoint {
  lazy val route: SecuredRoute[FooId, FooView] =
    securedEndpoint
      .summary("Get foo info")
      .get
      .in("api" / "v1" / "foos")
      .in(path[FooId])
      .out(jsonBody[FooView])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private[foos] case class FooView(id: FooId, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}