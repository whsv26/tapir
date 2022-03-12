package org.whsv26.tapir
package infrastructure.auth

import config.Config.JwtConfig
import org.whsv26.tapir.domain.users.Users.UserId
import domain.auth.{JwtClockAlgebra, JwtToken, JwtTokenAlgebra}
import infrastructure.auth.JwtTokenAlgebraInterpreter.UnableToDecodeJwtPrivateKey

import cats.MonadThrow
import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.SHA256withECDSA

import java.time.temporal.ChronoUnit

class JwtTokenAlgebraInterpreter[F[_]: Sync](
  conf: JwtConfig,
  clockAlg: JwtClockAlgebra[F],
) extends JwtTokenAlgebra[F] {

  override def generateToken(id: UserId): F[JwtToken] =
    for {
      clock <- clockAlg.utc

      privateKeyBytes <- MonadThrow[F].rethrow(Sync[F].delay {
        conf.privateKey
          .b64Bytes
          .toRight(UnableToDecodeJwtPrivateKey)
      })

      now = clock.instant()
      claims = JWTClaims()
        .withIssuer("whsv26")
        .withSubject(id.value.toString)
        .withIAT(now)
        .withExpiry(now.plus(7, ChronoUnit.DAYS))

      privateKey <- SHA256withECDSA.buildPrivateKey[F](privateKeyBytes)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)

    } yield JwtToken(jwtSignature.toEncodedString)
}

object JwtTokenAlgebraInterpreter {
  case object UnableToDecodeJwtPrivateKey extends Throwable
}
