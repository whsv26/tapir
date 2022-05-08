package org.whsv26.tapir
package infrastructure.endpoint

import domain.auth.{JwtToken, JwtTokenAlg}
import domain.users.UserId

import cats.Functor
import sttp.model.StatusCode

abstract class SecureApiEndpoint[F[_]: Functor](
  jwtTokenAlg: JwtTokenAlg[F]
) extends ApiEndpoint {

  protected final def securityLogic(token: JwtToken): F[Either[ErrorInfo, UserId]] =
    jwtTokenAlg
      .verifyToken(token)
      .leftMap(err => ErrorInfo(StatusCode.Unauthorized, err.cause))
      .value
}
