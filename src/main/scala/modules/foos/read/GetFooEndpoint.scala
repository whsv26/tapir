package org.whsv26.tapir
package modules.foos.read

import util.http.error.{ApiError, EntityNotFound}
import modules.foos.read.GetFooEndpoint.{FooView, NotFoundApiError}

import cats.data.OptionT
import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import org.whsv26.tapir.modules.auth.Tokens
import org.whsv26.tapir.modules.foos.{Foo, FooService}
import org.whsv26.tapir.util.http.security._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  tokens: Tokens[F]
) {
  lazy val route: SecuredServerRoute[F, Foo.Id, FooView] =
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
  lazy val route: SecuredRoute[Foo.Id, FooView] =
    securedEndpoint
      .summary("Get foo info")
      .get
      .in("api" / "v1" / "foos")
      .in(path[Foo.Id])
      .out(jsonBody[FooView])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private[foos] case class FooView(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}