package org.whsv26.tapir
package infrastructure.endpoint.jwt

import domain.auth.AuthService.{InvalidUserPassword, UserNotFoundByName}
import domain.auth.{AuthService, Token}
import domain.users.{PlainPassword, UserName}
import infrastructure.endpoint.ErrorInfo
import infrastructure.endpoint.ErrorInfo.User.{InvalidPassword, NotFoundByName}
import infrastructure.endpoint.jwt.CreateJwtTokenEndpoint.CreateJwtToken

import cats.effect.kernel.Sync
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint.Full

class CreateJwtTokenEndpoint[F[+_]: Sync](auth: AuthService[F]) {

  val action: Full[Unit, Unit, CreateJwtToken, ErrorInfo, Token, Any, F] =
    endpoint
      .summary("Sign in")
      .in("api" / "v1" / "token")
      .post
      .in(jsonBody[CreateJwtToken])
      .out(jsonBody[Token])
      .errorOut(statusCode
        .description(NotFoundByName.status, NotFoundByName.format)
        .description(InvalidPassword.status, InvalidPassword.format)
        .and(stringBody)
        .mapTo[ErrorInfo]
      )
      .serverLogic[F] { in => auth
        .signIn(UserName(in.name), PlainPassword(in.password))
        .leftMap {
          case UserNotFoundByName(name) => NotFoundByName(name)
          case InvalidUserPassword => InvalidPassword.apply
        }
        .value
      }
}

object CreateJwtTokenEndpoint {
  case class CreateJwtToken(
    name: String,
    password: String
  )
}