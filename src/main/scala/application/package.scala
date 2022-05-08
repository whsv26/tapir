package org.whsv26.tapir

import domain.auth.{Token, TokenAlg}
import domain.users.UserId

import cats.Functor
import sttp.model.StatusCode
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.{auth, endpoint, statusCode, stringBody}

package object application {
  type PartialSecuredRoute[F[_], IN, OUT] = PartialServerEndpoint[Token, UserId, IN, ApiError, OUT, Any, F]
  type SecuredRoute[F[_], IN, OUT] = Full[Token, UserId, IN, ApiError, OUT, Any, F]
  type PublicRoute[F[_], IN, OUT] = Full[Unit, Unit, IN, ApiError, OUT, Any, F]

  def securedEndpoint[F[_]: Functor](tokens: TokenAlg[F]): PartialSecuredRoute[F, Unit, Unit] =
    endpoint
      .securityIn(auth.bearer[Token]())
      .errorOut(
        statusCode
          .description(StatusCode.Unauthorized, "Unable to verify jwt-token")
          .and(stringBody)
          .mapTo[ApiError]
      )
      .serverSecurityLogic[UserId, F] { token =>
        tokens
          .verifyToken(token)
          .leftMap(err => ApiError(StatusCode.Unauthorized, err.cause))
          .value
      }


  case class ApiError(
    statusCode: StatusCode,
    msg: String
  )

  trait EntityApiError {
    def identity: String

    def name: String

    def action: String

    def status: StatusCode

    final val format: String = format(s"{$identity}")

    protected def format(identity: String) = s"$name $identity $action"

    final def apply(id: String): ApiError = ApiError(status, format(id))
  }

  class EntityNotFound(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    final val status: StatusCode = StatusCode.NotFound

    override def action = "not found"
  }

  class EntityAlreadyExists(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    final val status: StatusCode = StatusCode.Conflict

    override def action = "already exists"
  }
}
