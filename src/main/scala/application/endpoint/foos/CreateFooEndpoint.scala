package org.whsv26.tapir
package application.endpoint.foos

import application.endpoint.foos.CreateFooEndpoint._
import application.error.{ApiError, EntityAlreadyExists}
import application.security.{SecuredRoute, securedEndpoint, tokenAuth}
import domain.auth.{Token, TokenAlg}
import domain.foos.FooValidationAlg.FooAlreadyExists
import domain.foos.{FooId, FooService}

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class CreateFooEndpoint[F[_]: Sync](
  foos: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredRoute[F, CreateFoo, FooId] =
    CreateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => command =>
        foos.create(FooId.next, command)
          .leftMap { case FooAlreadyExists(id) => AlreadyExistsApiError(id.value.toString) }
          .value
      }
}

object CreateFooEndpoint {
  lazy val route: Endpoint[Token, CreateFoo, ApiError, FooId, Any] =
    securedEndpoint
      .summary("Create new foo")
      .post
      .in("api" / "v1" / "foo")
      .in(jsonBody[CreateFoo])
      .out(jsonBody[FooId])
      .errorOutVariants(
        oneOfVariant(AlreadyExistsApiError.out.mapTo[ApiError]),
      )

  final case class CreateFoo(a: NonNegInt, b: Boolean)

  object CreateFoo {
    implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
    implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
  }

  private object AlreadyExistsApiError extends EntityAlreadyExists("Foo")
}
