package org.whsv26.tapir
package modules.foos.read

import modules.auth.Tokens
import modules.foos.read.GetFooEndpoint.{NotFoundApiError, Response}
import modules.foos.{Foo, FooService}
import util.http.error.{ApiError, EntityNotFound}
import util.http.security._

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
  tokens: Tokens[F]
) {
  lazy val route: SecuredServerRoute[F, Foo.Id, Response] =
    GetFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => fooId =>
        OptionT(fooService.findById(fooId))
          .toRight(NotFoundApiError(fooId.value.toString))
          .map(_.transformInto[Response])
          .value
      }
}

object GetFooEndpoint {
  lazy val route: SecuredRoute[Foo.Id, Response] =
    securedEndpoint
      .summary("Get foo info")
      .get
      .in("foos")
      .in(path[Foo.Id])
      .out(jsonBody[Response])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private[read] case class Response(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}