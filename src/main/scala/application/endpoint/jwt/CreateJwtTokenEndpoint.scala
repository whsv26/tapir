package org.whsv26.tapir
package application.endpoint.jwt

import application.endpoint.jwt.CreateJwtTokenEndpoint.{CreateJwtToken, InvalidPasswordApiError, UserNotFoundApiError}
import application.error.{ApiError, ApiErrorLike, EntityNotFound}
import application.security.PublicRoute
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
  lazy val route: PublicRoute[F, CreateJwtToken, Token] =
    CreateJwtTokenEndpoint.route
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
  lazy val route: Endpoint[Unit, CreateJwtToken, ApiError, Token, Any] =
    endpoint
      .summary("Sign in")
      .in("api" / "v1" / "token")
      .post
      .in(jsonBody[CreateJwtToken])
      .out(jsonBody[Token])
      .errorOut((statusCode and stringBody).mapTo[ApiError])
      .errorOutVariants(
        oneOfVariant(UserNotFoundApiError.out.mapTo[ApiError]),
        oneOfVariant(InvalidPasswordApiError.out.mapTo[ApiError]),
      )

  case class CreateJwtToken(
    name: String,
    password: String
  )

  private object UserNotFoundApiError extends EntityNotFound("User", "name")
  private object InvalidPasswordApiError extends ApiErrorLike {
    val message: String = "Invalid password"
    val status: StatusCode = StatusCode.BadRequest
    def apply: ApiError = ApiError(status, message)
  }
}