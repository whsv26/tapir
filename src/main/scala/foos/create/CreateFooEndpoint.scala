package org.whsv26.tapir
package foos.create

import auth.Tokens
import foos.{CreateFooCommand, Foo}
import foos.FooValidation.FooAlreadyExists
import foos.create.CreateFooEndpoint._
import util.bus.Mediator
import util.http.error.{ApiError, EntityAlreadyExists}
import util.http.security._

import cats.data.EitherT
import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
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
      .in("api" / "v1" / "foos")
      .in(jsonBody[Request])
      .out(jsonBody[Response])
      .errorOutVariants(
        oneOfVariant(AlreadyExistsApiError.out.mapTo[ApiError]),
      )

  private[foos] case class Request(a: NonNegInt, b: Boolean)

  private[foos] case class Response(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object AlreadyExistsApiError extends EntityAlreadyExists("Foo")
}
