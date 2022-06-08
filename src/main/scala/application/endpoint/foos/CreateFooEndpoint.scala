package org.whsv26.tapir
package application.endpoint.foos

import application.endpoint.foos.CreateFooEndpoint._
import application.error.{ApiError, EntityAlreadyExists}
import application.security.{SecuredRoute, SecuredServerRoute, securedEndpoint, tokenAuth}
import domain.auth.TokenAlg
import domain.foos.FooValidationAlg.FooAlreadyExists
import domain.foos.{FooId, FooService}

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class CreateFooEndpoint[F[_]: Sync](
  foos: FooService[F],
  tokens: TokenAlg[F]
) {
  lazy val route: SecuredServerRoute[F, CreateFoo, CreatedFoo] =
    CreateFooEndpoint.route
      .serverSecurityLogic(tokenAuth(tokens))
      .serverLogic { _ => cmd =>
        foos.create(FooId.next, cmd)
          .map(foo => CreatedFoo(foo.id, foo.a, foo.b))
          .leftMap { case FooAlreadyExists(id) => AlreadyExistsApiError(id.value.toString) }
          .value
      }
}

object CreateFooEndpoint {
  lazy val route: SecuredRoute[CreateFoo, CreatedFoo] =
    securedEndpoint
      .summary("Create new foo")
      .post
      .in("api" / "v1" / "foos")
      .in(jsonBody[CreateFoo])
      .out(jsonBody[CreatedFoo])
      .errorOutVariants(
        oneOfVariant(AlreadyExistsApiError.out.mapTo[ApiError]),
      )

  final case class CreateFoo(a: NonNegInt, b: Boolean)
  final case class CreatedFoo(id: FooId, a: NonNegInt, b: Boolean)

  private object AlreadyExistsApiError extends EntityAlreadyExists("Foo")
}
