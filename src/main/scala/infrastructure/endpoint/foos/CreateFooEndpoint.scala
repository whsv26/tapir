package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.{JwtToken, JwtTokenAlg}
import domain.foos.FooValidationAlg.FooAlreadyExists
import domain.foos.{FooId, FooService}
import domain.users.UserId
import infrastructure.endpoint.ErrorInfo
import infrastructure.endpoint.ErrorInfo.Foo.AlreadyExists
import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo
import util.tapir.securedEndpoint

import cats.effect.kernel.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

import java.util.UUID

class CreateFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  jwtTokenAlg: JwtTokenAlg[F],
) {

  val action: Full[JwtToken, UserId, CreateFoo, ErrorInfo, FooId, Any, F] =
    securedEndpoint(jwtTokenAlg)
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
        fooService
          .create(FooId(UUID.randomUUID), command)
          .leftMap {
            case FooAlreadyExists(id) => AlreadyExists(id.value.toString)
          }
          .value
      }
}

object CreateFooEndpoint {
  final case class CreateFoo(a: Int, b: Boolean)

  object CreateFoo {
    implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
    implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
  }
}
