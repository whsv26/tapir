package org.whsv26.tapir
package util

import domain.auth.{Token, TokenAlg}
import domain.users.UserId
import infrastructure.endpoint.ErrorInfo

import cats.Functor
import sttp.model.StatusCode
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.{auth, endpoint, statusCode, stringBody}

package object tapir {
  type PartialSecuredRoute[F[_], IN, OUT] = PartialServerEndpoint[Token, UserId, IN, ErrorInfo, OUT, Any, F]
  type SecuredRoute[F[_], IN, OUT] = Full[Token, UserId, IN, ErrorInfo, OUT, Any, F]
  type PublicRoute[F[_], IN, OUT] = Full[Unit, Unit, IN, ErrorInfo, OUT, Any, F]

  def securedEndpoint[F[_]: Functor](tokens: TokenAlg[F]): PartialSecuredRoute[F, Unit, Unit] =
    endpoint
      .securityIn(auth.bearer[Token]())
      .errorOut(
        statusCode
          .description(StatusCode.Unauthorized, "Unable to verify jwt-token")
          .and(stringBody)
          .mapTo[ErrorInfo]
      )
      .serverSecurityLogic[UserId, F] { token =>
        tokens
          .verifyToken(token)
          .leftMap(err => ErrorInfo(StatusCode.Unauthorized, err.cause))
          .value
      }
}
