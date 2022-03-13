package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.foos.Foo.FooId
import domain.foos.FooService
import domain.foos.FooValidationAlgebra.FooAlreadyExists
import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo
import infrastructure.endpoint.{ApiEndpoint, ErrorInfo}

import cats.effect.kernel.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full
import java.util.UUID

class CreateFooEndpoint[F[_]: Sync](
  fooService: FooService[F]
) extends ApiEndpoint {

  private val action = endpoint
    .in(prefix / "foo")
    .post
    .in(jsonBody[CreateFoo])
    .out(jsonBody[FooId])
    .errorOut(statusCode
      .description(StatusCode.Conflict, "Already exists")
      .and(stringBody)
      .mapTo[ErrorInfo]
    )
    .serverLogic[F] { command =>
      fooService
        .create(UUID.randomUUID, command)
        .leftMap {
          case FooAlreadyExists(id) => ErrorInfo(
            StatusCode.Conflict,
            s"Foo $id already exists"
          )
        }
        .value
    }
}

object CreateFooEndpoint {
  def apply[F[_]: Sync](fooService: FooService[F]): Full[Unit, Unit, CreateFoo, ErrorInfo, FooId, Any, F] =
    new CreateFooEndpoint[F](fooService).action

  final case class CreateFoo(a: Int, b: Boolean)

  object CreateFoo {
    implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
    implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
  }
}
