package org.whsv26.tapir
package infrastructure.endpoint.jwt

import domain.auth.AuthService
import domain.auth.AuthService.{InvalidPassword, UserNotFound}
import domain.users.Users.{PlainPassword, UserName}
import infrastructure.endpoint.ApiEndpoint
import infrastructure.endpoint.jwt.CreateJwtTokenEndpoint.CreateJwtToken
import cats.effect.kernel.Sync
import io.circe.generic.auto._
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
    .errorOut(jsonBody[String])
    .serverLogic[F] { in =>
      auth.signIn(UserName(in.name), PlainPassword(in.password))
        .map(_.token)
        .leftMap {
          case UserNotFound(name) => s"User with name '$name' is not found"
          case InvalidPassword => "Invalid password"
        }
        .value
    }
}

object CreateJwtTokenEndpoint {
  def apply[F[+_]: Sync](auth: AuthService[F]): Full[Unit, Unit, CreateJwtToken, String, String, Any, F] =
    new CreateJwtTokenEndpoint[F](auth).action

  case class CreateJwtToken(
    name: String,
    password: String
  )
}