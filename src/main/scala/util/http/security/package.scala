package org.whsv26.tapir
package util.http

import util.http.error.ApiError

import cats.Functor
import org.whsv26.tapir.auth.{Tokens, User}
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{Endpoint, auth, endpoint, statusCode, stringBody}

/// TODO: User dependency!
package object security {
  type SecuredServerRoute[F[_], IN, OUT] = ServerEndpoint.Full[User.Token, User.Id, IN, ApiError, OUT, Any, F]
  type SecuredRoute[IN, OUT] = Endpoint[User.Token, IN, ApiError, OUT, Any]
  type PublicServerRoute[F[_], IN, OUT] = ServerEndpoint.Full[Unit, Unit, IN, ApiError, OUT, Any, F]
  type PublicRoute[IN, OUT] = Endpoint[Unit, IN, ApiError, OUT, Any]
  type ServerEndpoints[F[_]] = List[ServerEndpoint[Any, F]]
  type Endpoints = List[Endpoint[_, _, _, _, _]]

  val securedEndpoint: Endpoint[User.Token, Unit, ApiError, Unit, Any] =
    endpoint
      .securityIn(auth.bearer[User.Token]())
      .errorOut(
        statusCode
          .description(StatusCode.Unauthorized, "Unable to verify bearer token")
          .and(stringBody)
          .mapTo[ApiError]
      )

  def tokenAuth[F[_]: Functor](
    tokens: Tokens[F]
  ): User.Token => F[Either[ApiError, User.Id]] =
    token =>
      tokens.verifyToken(token)
        .leftMap(err => ApiError(StatusCode.Unauthorized, err.cause))
        .value
}
