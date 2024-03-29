package org.whsv26.tapir
package modules.foos.create

import modules.foos.{CreateFooCommand, Foo}
import modules.foos.FooValidation.FooAlreadyExists
import modules.foos.create.CreateFooEndpoint._
import util.bus.Mediator
import util.http.error.{ApiError, EntityAlreadyExists}
import util.http.security._

import cats.data.EitherT
import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import org.whsv26.tapir.modules.auth.Tokens
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class CreateFooEndpoint[F[_]: Sync](
  mediator: Mediator[F],
  tokens: Tokens[F]
) {
  lazy val route: SecuredServerRoute[F, Request, Response] =
    CreateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => request =>
        val command = request
          .into[CreateFooCommand]
          .withFieldConst(_.id, Foo.Id.next)
          .transform

        EitherT(mediator.send(command))
          .map(_.transformInto[Response])
          .leftMap { case FooAlreadyExists(id) => AlreadyExistsApiError(id.value.toString) }
          .value
      }
}

object CreateFooEndpoint {
  lazy val route: SecuredRoute[Request, Response] =
    securedEndpoint
      .summary("Create new foo")
      .post
      .in("foos")
      .in(jsonBody[Request])
      .out(jsonBody[Response])
      .errorOutVariants(
        oneOfVariant(AlreadyExistsApiError.out.mapTo[ApiError]),
      )

  private[create] case class Request(a: NonNegInt, b: Boolean)

  private[create] case class Response(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object AlreadyExistsApiError extends EntityAlreadyExists("Foo")
}
