package org.whsv26.tapir

import cats.effect.std.Console
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Sync}
import cats.implicits._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.SHA256withECDSA

import java.security.{PrivateKey, PublicKey}
import java.util.UUID

class JwtTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "generated JWT-token must be verifiable" in {
    val res = for {
      pair <- SHA256withECDSA.generateKeyPair[IO]
      publicKey = pair.publicKey.asInstanceOf[PublicKey]
      privateKey = pair.privateKey.asInstanceOf[PrivateKey]
      generatedToken <- generateJwtToken[IO](privateKey)
      encodedToken = generatedToken.toEncodedString
      verifiedToken <- verifyJwtToken[IO](encodedToken, publicKey)
    } yield (generatedToken, verifiedToken)

    res.asserting { case (generated, verified) =>
      assertResult(generated.toEncodedString)(verified.toEncodedString)
    }
  }

  def generateJwtToken[F[_]: Sync](privateKey: PrivateKey): F[JWTSig[SHA256withECDSA]] =
    for {
      priKey <- SHA256withECDSA.buildPrivateKey[F](privateKey.getEncoded)
      claims = JWTClaims(subject = UUID.randomUUID().toString.some)
      jwtSig <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, priKey)
    } yield jwtSig

  def verifyJwtToken[F[_]: Sync: Console](
    token: String,
    publicKey: PublicKey
  ): F[JWTSig[SHA256withECDSA]] =
    for {
      pubKey <- SHA256withECDSA.buildPublicKey[F](publicKey.getEncoded)
      verified <- JWTSig.verifyK[F, SHA256withECDSA](token, pubKey)
    } yield verified
}
