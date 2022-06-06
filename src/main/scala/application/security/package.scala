package org.whsv26.tapir
package application

import application.error.ApiError
import domain.auth.{Token, TokenAlg}
import domain.users.UserId

import cats.Functor
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{Endpoint, auth, endpoint, statusCode, stringBody}

package object security {
  type SecuredServerRoute[F[_], IN, OUT] = ServerEndpoint.Full[Token, UserId, IN, ApiError, OUT, Any, F]
  type SecuredRoute[IN, OUT] = Endpoint[Token, IN, ApiError, OUT, Any]
  type PublicServerRoute[F[_], IN, OUT] = ServerEndpoint.Full[Unit, Unit, IN, ApiError, OUT, Any, F]
  type PublicRoute[IN, OUT] = Endpoint[Unit, IN, ApiError, OUT, Any]
  type ServerEndpoints[F[_]] = List[ServerEndpoint[Any, F]]
  type Endpoints = List[Endpoint[_, _, _, _, _]]

  val securedEndpoint: Endpoint[Token, Unit, ApiError, Unit, Any] =
    endpoint
      .securityIn(auth.bearer[Token]())
      .errorOut(
        statusCode
          .description(StatusCode.Unauthorized, "Unable to verify jwt-token")
          .and(stringBody)
          .mapTo[ApiError]
      )

  def tokenAuth[F[_]: Functor](
    tokens: TokenAlg[F]
  ): Token => F[Either[ApiError, UserId]] =
    token =>
      tokens.verifyToken(token)
        .leftMap(err => ApiError(StatusCode.Unauthorized, err.cause))
        .value
}
