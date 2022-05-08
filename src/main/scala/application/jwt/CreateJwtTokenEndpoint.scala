package org.whsv26.tapir
package application.jwt

import application.jwt.CreateJwtTokenEndpoint.{CreateJwtToken, InvalidPasswordApiError, UserNotFoundApiError}
import application.{ApiError, EntityNotFound, PublicRoute}
import domain.auth.AuthService.{InvalidPassword, UserNotFound}
import domain.auth.{AuthService, Token}
import domain.users.{PlainPassword, UserName}

import cats.effect.kernel.Sync
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody

class CreateJwtTokenEndpoint[F[+_]: Sync](auth: AuthService[F]) {

  val route: PublicRoute[F, CreateJwtToken, Token] =
    endpoint
      .summary("Sign in")
      .in("api" / "v1" / "token")
      .post
      .in(jsonBody[CreateJwtToken])
      .out(jsonBody[Token])
      .errorOut(statusCode
        .description(UserNotFoundApiError.status, UserNotFoundApiError.format)
        .description(InvalidPasswordApiError.status, InvalidPasswordApiError.format)
        .and(stringBody)
        .mapTo[ApiError]
      )
      .serverLogic[F] { in => auth
        .signIn(UserName(in.name), PlainPassword(in.password))
        .leftMap {
          case UserNotFound(name) => UserNotFoundApiError(name)
          case InvalidPassword => InvalidPasswordApiError.apply
        }
        .value
      }
}

object CreateJwtTokenEndpoint {
  case class CreateJwtToken(
    name: String,
    password: String
  )

  private object UserNotFoundApiError extends EntityNotFound("User", "name")
  private object InvalidPasswordApiError {
    val format: String = "Invalid password"
    val status: StatusCode = StatusCode.BadRequest
    def apply: ApiError = ApiError(status, format)
  }
}