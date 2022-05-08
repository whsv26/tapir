package org.whsv26.tapir
package application

import application.error.ApiError
import domain.auth.{Token, TokenAlg}
import domain.users.UserId

import cats.Functor
import sttp.model.StatusCode
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.{auth, endpoint, statusCode, stringBody}

package object security {
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
}
