package org.whsv26.tapir
package infrastructure.endpoint.foos

import domain.auth.{JwtToken, JwtTokenAlg}
import domain.foos.{Foo, FooId, FooService}
import domain.users.UserId
import infrastructure.endpoint.{ErrorInfo, SecureApiEndpoint}

import cats.effect.kernel.Sync
import cats.syntax.functor._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class GetFooEndpoint[F[_]: Sync](
  fooService: FooService[F],
  jwtTokenAlg: JwtTokenAlg[F],
) extends SecureApiEndpoint[F](jwtTokenAlg) {

  val action: Full[JwtToken, UserId, FooId, ErrorInfo, Foo, Any, F] =
    endpoint
      .in(prefix / "foo")
      .get
      .in(jsonBody[FooId])
      .out(jsonBody[Foo])
      .errorOut(statusCode
        .description(StatusCode.NotFound, "Not found")
        .and(stringBody)
        .mapTo[ErrorInfo]
      )
      .securityIn(auth.bearer[JwtToken]())
      .serverSecurityLogic[UserId, F](securityLogic)
      .serverLogic { _ => fooId =>
        fooService
          .findById(fooId)
          .map(_.toRight(ErrorInfo(
            StatusCode.Conflict,
            s"Foo ${fooId.value} not found"
          )))
      }
}