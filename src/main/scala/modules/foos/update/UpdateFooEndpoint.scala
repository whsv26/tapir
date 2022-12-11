package org.whsv26.tapir
package modules.foos.update

import modules.auth.Tokens
import modules.foos.FooValidation.FooDoesNotExist
import modules.foos.update.UpdateFooEndpoint.{NotFoundApiError, Request, Response}
import modules.foos.{Foo, FooService}
import util.http.error.{ApiError, EntityNotFound}
import util.http.security._

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class UpdateFooEndpoint[F[_]: Sync](
  foos: FooService[F],
  tokens: Tokens[F]
) {
  lazy val route: SecuredServerRoute[F, (Foo.Id, Request), Response] =
    UpdateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => { case (id, cmd) =>
        foos.update(Foo(id, cmd.a, cmd.b))
          .map(_.transformInto[Response])
          .leftMap { case FooDoesNotExist(id) => NotFoundApiError(id.value.toString) }
          .value
      }}
}

object UpdateFooEndpoint {
  lazy val route: SecuredRoute[(Foo.Id, Request), Response] =
    securedEndpoint
      .summary("Update existing foo")
      .patch
      .in("foos")
      .in(path[Foo.Id]("Foo.Id"))
      .in(jsonBody[Request])
      .out(jsonBody[Response])
      .errorOutVariants(
        oneOfVariant(NotFoundApiError.out.mapTo[ApiError]),
      )

  private[update] case class Request(a: NonNegInt, b: Boolean)

  private[update] case class Response(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object NotFoundApiError extends EntityNotFound("Foo")
}
