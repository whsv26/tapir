package org.whsv26.tapir
package infrastructure.auth

import config.Config.JwtConfig
import domain.auth.JwtTokenAlg.JwtTokenVerificationError
import domain.auth.{JwtClockAlg, JwtToken, JwtTokenAlg}
import domain.users.UserId
import infrastructure.auth.JwtTokenInterpreter.UnableToDecodeJwtPrivateKey

import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{GeneralSignatureError, SHA256withECDSA}

import java.time.temporal.ChronoUnit
import java.util.UUID

class JwtTokenInterpreter[F[_]: Sync](
  conf: JwtConfig,
  clockAlg: JwtClockAlg[F],
) extends JwtTokenAlg[F] {

  override def verifyToken(token: JwtToken): EitherT[F, JwtTokenVerificationError, UserId] = {
    val pubKeyBytes = conf.publicKey
      .b64Bytes
      .getOrElse(Array.empty)

    val verified = for {
      pubKey <- SHA256withECDSA.buildPublicKey[F](pubKeyBytes)
      verified <- JWTSig.verifyK[F, SHA256withECDSA](token.value, pubKey)
      userId = verified.body.subject.getOrElse("")
    } yield UserId(UUID.fromString(userId))

    EitherT {
      verified
        .adaptError { case GeneralSignatureError(s) => JwtTokenVerificationError(s) }
        .attemptNarrow[JwtTokenVerificationError]
    }
  }

  override def generateToken(id: UserId): F[JwtToken] =
    for {
      privateKeyBytes <- Sync[F].delay {
        conf.privateKey.b64Bytes.toRight(UnableToDecodeJwtPrivateKey)
      }.rethrow
      privateKey <- SHA256withECDSA.buildPrivateKey[F](privateKeyBytes)
      claims <- buildClaims(id)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)
    } yield JwtToken(jwtSignature.toEncodedString)

  private def buildClaims(id: UserId): F[JWTClaims] =
    for {
      clock <- clockAlg.utc
      issuedAt <- Sync[F].delay(clock.instant())
      expiredAt = issuedAt.plus(conf.expiry.value, ChronoUnit.SECONDS)
    } yield JWTClaims()
      .withIssuer(conf.issuer)
      .withSubject(id.value.toString)
      .withIAT(issuedAt)
      .withExpiry(expiredAt)
}

object JwtTokenInterpreter {
  case object UnableToDecodeJwtPrivateKey extends Throwable
}
