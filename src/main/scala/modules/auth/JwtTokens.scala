package org.whsv26.tapir
package modules.auth

import config.Config.JwtConfig
import modules.auth.JwtTokens.{UnableToDecodeJwtPrivateKey, UnableToDecodeJwtPublicKey}
import modules.auth.Tokens.UnableToVerifyToken
import util.time.Clock

import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{GeneralSignatureError, SHA256withECDSA, SigPrivateKey, SigPublicKey}

import java.time.temporal.ChronoUnit
import java.util.UUID

class JwtTokens[F[_]: Sync](
  conf: JwtConfig,
  clock: Clock[F],
) extends Tokens[F] {

  override def verifyToken(token: User.Token): EitherT[F, UnableToVerifyToken.type, User.Id] = {
    val verify = for {
      publicKey <- buildPublicKey()
      verified <- JWTSig.verifyK[F, SHA256withECDSA](token.value, publicKey)
      userId = verified.body.subject.getOrElse("")
    } yield User.Id(UUID.fromString(userId))

    EitherT {
      verify
        .adaptError { case GeneralSignatureError(_) => UnableToVerifyToken }
        .attemptNarrow[UnableToVerifyToken.type]
    }
  }

  override def generateToken(id: User.Id): F[User.Token] =
    for {
      privateKey <- buildPrivateKey()
      claims <- buildClaims(id)
      jwtSignature <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, privateKey)
    } yield User.Token(jwtSignature.toEncodedString)

  private def buildPublicKey(): F[SigPublicKey[SHA256withECDSA]] =
    Sync[F]
      .fromEither(conf.publicKey.b64Bytes.toRight(UnableToDecodeJwtPublicKey))
      .flatMap(SHA256withECDSA.buildPublicKey[F])

  private def buildPrivateKey(): F[SigPrivateKey[SHA256withECDSA]] =
    Sync[F]
      .fromEither(conf.privateKey.b64Bytes.toRight(UnableToDecodeJwtPrivateKey))
      .flatMap(SHA256withECDSA.buildPrivateKey[F])

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
  case object UnableToDecodeJwtPublicKey extends Throwable
}
