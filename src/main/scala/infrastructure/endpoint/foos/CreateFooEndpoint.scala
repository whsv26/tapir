package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.foos.Foo.FooId
import domain.foos.FooService
import domain.foos.FooValidationAlgebra.FooAlreadyExists
import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo

import cats.effect.kernel.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

import java.util.UUID

class CreateFooEndpoint[F[_]: Sync](fooService: FooService[F]) {
  type ErrorInfo = String

  private val createFoo = endpoint
    .in("api" / "v1" / "foo")
    .post
    .in(jsonBody[CreateFoo])
    .out(jsonBody[FooId])
    .errorOut(jsonBody[ErrorInfo].description("Already exists"))
    .serverLogic[F] { command =>
      fooService
        .create(UUID.randomUUID, command)
        .leftMap {
          case FooAlreadyExists(id) => s"Foo $id already exists"
        }
        .value
    }
}

object CreateFooEndpoint {
  def apply[F[_]: Sync](fooService: FooService[F]): Full[Unit, Unit, CreateFoo, String, FooId, Any, F] =
    new CreateFooEndpoint[F](fooService).createFoo

  final case class CreateFoo(a: Int, b: Boolean)

  object CreateFoo {
    implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
    implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
  }
}
