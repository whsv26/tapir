package org.whsv26.tapir
package infrastructure.endpoint.jwt

import domain.auth.AuthService
import domain.auth.AuthService.{InvalidPassword, UserNotFound}
import domain.users.Users.{PlainPassword, UserName}
import infrastructure.endpoint.{ApiEndpoint, ErrorInfo}
import infrastructure.endpoint.jwt.CreateJwtTokenEndpoint.CreateJwtToken
import cats.effect.kernel.Sync
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class CreateJwtTokenEndpoint[F[+_]: Sync](
  auth: AuthService[F]
) extends ApiEndpoint {

  private val action = endpoint
    .in(prefix / "token")
    .post
    .in(jsonBody[CreateJwtToken])
    .out(jsonBody[String])
    .errorOut(statusCode
      .description(StatusCode.NotFound, "User not found")
      .description(StatusCode.BadRequest, "Invalid password")
      .and(stringBody)
      .mapTo[ErrorInfo]
    )
    .serverLogic[F] { in => auth
      .signIn(UserName(in.name), PlainPassword(in.password))
      .map(_.token)
      .leftMap {
        case UserNotFound(name) => ErrorInfo(
          StatusCode.NotFound,
          s"User with name '$name' is not found"
        )
        case InvalidPassword => ErrorInfo(
          StatusCode.BadRequest,
          "Invalid password"
        )
      }
      .value
    }
}

object CreateJwtTokenEndpoint {
  def apply[F[+_]: Sync](auth: AuthService[F]): Full[Unit, Unit, CreateJwtToken, ErrorInfo, String, Any, F] =
    new CreateJwtTokenEndpoint[F](auth).action

  case class CreateJwtToken(
    name: String,
    password: String
  )
}