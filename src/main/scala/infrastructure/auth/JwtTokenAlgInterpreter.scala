package org.whsv26.tapir
package infrastructure.auth

import config.Config.JwtConfig
import domain.auth.TokenAlg.TokenVerificationError
import domain.auth.{JwtClockAlg, Token, TokenAlg}
import domain.users.UserId
import infrastructure.auth.JwtTokenAlgInterpreter.UnableToDecodeJwtPrivateKey

import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{GeneralSignatureError, SHA256withECDSA}

import java.time.temporal.ChronoUnit
import java.util.UUID

class JwtTokenAlgInterpreter[F[_]: Sync](
  conf: JwtConfig,
  clockAlg: JwtClockAlg[F],
) extends TokenAlg[F] {

  override def verifyToken(token: Token): EitherT[F, TokenVerificationError, UserId] = {
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
        .adaptError { case GeneralSignatureError(s) => TokenVerificationError(s) }
        .attemptNarrow[TokenVerificationError]
    }
  }

  override def generateToken(id: UserId): F[Token] =
    for {
      privateKeyBytes <- Sync[F].delay {
        conf.privateKey.b64Bytes.toRight(UnableToDecodeJwtPrivateKey)
      }.rethrow
      privateKey <- SHA256withECDSA.buildPrivateKey[F](privateKeyBytes)
      claims <- buildClaims(id)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)
    } yield Token(jwtSignature.toEncodedString)

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

object JwtTokenAlgInterpreter {
  case object UnableToDecodeJwtPrivateKey extends Throwable
}
