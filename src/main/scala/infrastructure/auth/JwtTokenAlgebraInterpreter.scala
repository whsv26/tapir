package org.whsv26.tapir
package infrastructure.auth

import config.Config.JwtConfig
import domain.auth.{JwtClockAlgebra, JwtToken, JwtTokenAlgebra}
import domain.users.Users.UserId
import infrastructure.auth.JwtTokenAlgebraInterpreter.UnableToDecodeJwtPrivateKey

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._
import org.whsv26.tapir.domain.auth.JwtTokenAlgebra.JwtTokenVerificationError
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{GeneralSignatureError, SHA256withECDSA}

import java.time.temporal.ChronoUnit
import java.util.UUID

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

      issuedAt = clock.instant()
      expiredAt = issuedAt.plus(conf.expiry, ChronoUnit.SECONDS)

      claims = JWTClaims()
        .withIssuer(conf.issuer)
        .withSubject(id.value.toString)
        .withIAT(issuedAt)
        .withExpiry(expiredAt)

      privateKey <- SHA256withECDSA.buildPrivateKey[F](privateKeyBytes)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)

    } yield JwtToken(jwtSignature.toEncodedString)

    override def verifyToken(token: JwtToken): EitherT[F, JwtTokenVerificationError, UserId] = {
      val pubKeyBytes = conf.publicKey
        .b64Bytes
        .getOrElse(Array.empty)

      val verified = for {
        pubKey <- SHA256withECDSA.buildPublicKey[F](pubKeyBytes)
        verified <- JWTSig.verifyK[F, SHA256withECDSA](token.value, pubKey)
          .adaptError {
            case GeneralSignatureError(cause) => JwtTokenVerificationError(cause)
          }
        userId = verified.body.subject.getOrElse("")
      } yield UserId(UUID.fromString(userId))

      EitherT(verified.attemptNarrow[JwtTokenVerificationError])
    }
}

object JwtTokenAlgebraInterpreter {
  case object UnableToDecodeJwtPrivateKey extends Throwable
}
