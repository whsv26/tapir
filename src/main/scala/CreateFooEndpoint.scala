package org.whsv26.tapir

import FooService.FooAlreadyExists

import cats.effect.kernel.Sync
import cats.implicits._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full
import java.util.UUID

class CreateFooEndpoint[F[_]: Sync](fooService: FooService[F]) {
  private val createFoo = endpoint
    .in("api" / "v1" / "foo")
    .post
    .in(jsonBody[Foo])
    .out(jsonBody[UUID])
    .errorOut(jsonBody[String])
    .serverLogic[F] { foo =>
      fooService
        .create(foo)
        .attempt
        .map(_.leftMap {
          case FooAlreadyExists(foo) => s"Foo with id ${foo.id} already exists"
          case err: Throwable => err.getMessage
        })
    }
}

object CreateFooEndpoint {
  def apply[F[_]: Sync](fooService: FooService[F]): Full[Unit, Unit, Foo, String, UUID, Any, F] =
    new CreateFooEndpoint[F](fooService).createFoo
}
