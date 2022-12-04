package org.whsv26.tapir
package foos.create

import util.http.error.{ApiError, EntityAlreadyExists}

import org.whsv26.tapir.foos.FooValidationAlg.FooAlreadyExists
import foos.create.CreateFooEndpoint._

import cats.effect.kernel.Sync
import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.auto._
import io.circe.refined._
import io.scalaland.chimney.dsl.TransformerOps
import org.whsv26.tapir.auth.TokenAlg
import org.whsv26.tapir.foos.{Foo, FooService}
import org.whsv26.tapir.util.http.security._
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
        foos.create(Foo(Foo.Id.next, cmd.a, cmd.b))
          .map(_.transformInto[CreatedFoo])
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

  private[foos] case class CreateFoo(a: NonNegInt, b: Boolean)

  private[foos] case class CreatedFoo(id: Foo.Id, a: NonNegInt, b: Boolean)

  private object AlreadyExistsApiError extends EntityAlreadyExists("Foo")
}
