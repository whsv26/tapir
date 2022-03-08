package org.whsv26.tapir

import Foo.FooId
import FooValidationAlgebra.FooAlreadyExists
import cats.effect.kernel.Sync
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class CreateFooEndpoint[F[_]: Sync](fooService: FooService[F]) {
  type ErrorInfo = String

  private val createFoo = endpoint
    .in("api" / "v1" / "foo")
    .post
    .in(jsonBody[Foo])
    .out(jsonBody[FooId])
    .errorOut(jsonBody[ErrorInfo].description("Already exists"))
    .serverLogic[F] { foo =>
      fooService
        .create(foo)
        .leftMap {
          case FooAlreadyExists(id) => s"Foo $id already exists"
        }
        .value
    }
}

object CreateFooEndpoint {
  def apply[F[_]: Sync](fooService: FooService[F]): Full[Unit, Unit, Foo, String, FooId, Any, F] =
    new CreateFooEndpoint[F](fooService).createFoo
}
