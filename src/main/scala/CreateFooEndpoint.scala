package org.whsv26.tapir

import Foo.FooId
import FooValidationAlgebra.FooAlreadyExists

import cats.effect.kernel.Sync
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
}
