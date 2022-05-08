package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.TokenAlg
import domain.foos.FooValidationAlg.FooAlreadyExists
import domain.foos.{FooId, FooService}
import infrastructure.endpoint.ErrorInfo
import infrastructure.endpoint.ErrorInfo.Foo.AlreadyExists
import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo
import util.tapir.{SecuredRoute, securedEndpoint}

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
  tokens: TokenAlg[F],
) {

  val route: SecuredRoute[F, CreateFoo, FooId] =
    securedEndpoint(tokens)
      .summary("Create new foo")
      .post
      .in("api" / "v1" / "foo")
      .in(jsonBody[CreateFoo])
      .out(jsonBody[FooId])
      .errorOutVariant(oneOfVariant(
        statusCode
          .description(AlreadyExists.status, AlreadyExists.format)
          .and(stringBody)
          .mapTo[ErrorInfo]
      ))
      .serverLogic { _ => command =>
        foos.create(FooId.next, command)
          .leftMap { case FooAlreadyExists(id) => AlreadyExists(id.value.toString) }
          .value
      }
}

object CreateFooEndpoint {
  final case class CreateFoo(a: NonNegInt, b: Boolean)

  object CreateFoo {
    implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
    implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
  }
}
