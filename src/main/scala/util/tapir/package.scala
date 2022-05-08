package org.whsv26.tapir
package util

import domain.auth.{JwtToken, JwtTokenAlg}
import domain.users.UserId
import infrastructure.endpoint.ErrorInfo

import cats.Functor
import sttp.model.StatusCode
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.{auth, endpoint, statusCode, stringBody}

package object tapir {
  type SecuredEndpoint[F[_]] = PartialServerEndpoint[JwtToken, UserId, Unit, ErrorInfo, Unit, Any, F]

  def securedEndpoint[F[_]: Functor](tokens: JwtTokenAlg[F]): SecuredEndpoint[F] =
    endpoint
      .securityIn(auth.bearer[JwtToken]())
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
