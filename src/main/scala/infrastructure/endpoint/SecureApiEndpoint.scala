package org.whsv26.tapir
package infrastructure.endpoint

import domain.auth.{JwtToken, JwtTokenAlgebra}
import domain.users.Users.UserId
import cats.Functor
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.server.PartialServerEndpoint

abstract class SecureApiEndpoint[F[_]: Functor](
  jwtTokenAlg: JwtTokenAlgebra[F]
) extends ApiEndpoint {

  protected final def securityLogic(token: JwtToken): F[Either[ErrorInfo, UserId]] =
    jwtTokenAlg
      .verifyToken(token)
      .leftMap(err => ErrorInfo(StatusCode.Unauthorized, err.cause))
      .value

  /**
   * With this approach, OpenAPI error discrimination is impossible
   * todo think about alternatives
   */
  protected val secureEndpoint: PartialServerEndpoint[JwtToken, UserId, Unit, ErrorInfo, Unit, Any, F] =
    endpoint
      .securityIn(auth.bearer[JwtToken]())
      .errorOut(statusCode.and(stringBody).mapTo[ErrorInfo])
      .serverSecurityLogic[UserId, F] { token =>
        jwtTokenAlg
          .verifyToken(token)
          .leftMap(err => ErrorInfo(StatusCode.Unauthorized, err.cause))
          .value
      }
}
