package org.whsv26.tapir
package modules.auth

import config.Config.JwtConfig
import modules.auth.JwtTokens.UnableToDecodeJwtPrivateKey
import modules.auth.Tokens.TokenVerificationError
import util.time.Clock

import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{GeneralSignatureError, SHA256withECDSA}

import java.time.temporal.ChronoUnit
import java.util.UUID

// TODO review error handling
class JwtTokens[F[_]: Sync](
  conf: JwtConfig,
  clock: Clock[F],
) extends Tokens[F] {

  override def verifyToken(token: User.Token): EitherT[F, TokenVerificationError, User.Id] = {
    val pubKeyBytes = conf.publicKey
      .b64Bytes
      .getOrElse(Array.empty)

    val verified = for {
      pubKey <- SHA256withECDSA.buildPublicKey[F](pubKeyBytes)
      verified <- JWTSig.verifyK[F, SHA256withECDSA](token.value, pubKey)
      userId = verified.body.subject.getOrElse("")
    } yield User.Id(UUID.fromString(userId))

    EitherT {
      verified
        .adaptError { case GeneralSignatureError(s) => TokenVerificationError(s) }
        .attemptNarrow[TokenVerificationError]
    }
  }

  override def generateToken(id: User.Id): F[User.Token] =
    for {
      privateKeyBytes <- Sync[F].delay {
        conf.privateKey.b64Bytes.toRight(UnableToDecodeJwtPrivateKey)
      }.rethrow
      privateKey <- SHA256withECDSA.buildPrivateKey[F](privateKeyBytes)
      claims <- buildClaims(id)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)
    } yield User.Token(jwtSignature.toEncodedString)

  private def buildClaims(id: User.Id): F[JWTClaims] =
    for {
      issuedAt <- clock.now
      expiredAt = issuedAt.plus(conf.expiry.toSeconds, ChronoUnit.SECONDS)
    } yield JWTClaims()
      .withIssuer(conf.issuer)
      .withSubject(id.value.toString)
      .withIAT(issuedAt)
      .withExpiry(expiredAt)
}

object JwtTokens {
  case object UnableToDecodeJwtPrivateKey extends Throwable
}
