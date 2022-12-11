package org.whsv26.tapir

import cats.Monad
import cats.effect.std.Console
import cats.effect.{IO, IOApp, Sync}
import cats.implicits._
import tsec.common._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{SHA256withECDSA, SigKeyPair}

import java.security.{Key, PrivateKey, PublicKey}
import java.util.UUID

/**
 * TODO: make it actual test
 * Verify JWT signature via [[https://jwt.io/]]
 */
object JwtPemTestApp extends IOApp.Simple {
  def toPem(key: Key): String = {
    val der = key.getEncoded // Distinguished Encoding Rules
    val derB64 = der.toB64String

    val keyType = key match {
      case _: PublicKey => "PUBLIC"
      case _: PrivateKey => "PRIVATE"
    }

    val prefix = s"-----BEGIN $keyType KEY-----"
    val suffix = s"-----END $keyType KEY-----"

    prefix + "\n" + derB64 + "\n" + suffix
  }

  def showPem[F[_]: Console: Monad](
    publicKey: PublicKey,
    privateKey: PrivateKey
  ): F[Unit] =
    for {
      _ <- Console[F].println(toPem(publicKey))
      _ <- Console[F].println("")
      _ <- Console[F].println(toPem(privateKey))
      _ <- Console[F].println("")
    } yield ()

  def generateKeyPair[F[_]: Sync: Console]: F[SigKeyPair[SHA256withECDSA]] =
    for {
      keyPair <- SHA256withECDSA.generateKeyPair[F]
      pubKey = keyPair.publicKey.asInstanceOf[PublicKey]
      priKey = keyPair.privateKey.asInstanceOf[PrivateKey]
      _ <- showPem(pubKey, priKey)
    } yield keyPair

  def buildJwtToken[F[_]: Sync](
    claims: JWTClaims,
    privateKey: PrivateKey
  ): F[JWTSig[SHA256withECDSA]] =
    for {
      priKey <- SHA256withECDSA.buildPrivateKey[F](privateKey.getEncoded)
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

  override def run: IO[Unit] =
    for {
      pair <- generateKeyPair[IO]
      publicKey = pair.publicKey.asInstanceOf[PublicKey]
      privateKey = pair.privateKey.asInstanceOf[PrivateKey]
      claims = JWTClaims(subject = UUID.randomUUID().toString.some)
      token <- buildJwtToken[IO](claims, privateKey)
      _ <- verifyJwtToken[IO](token.toEncodedString, publicKey)
      _ <- IO.println(token.toEncodedString)
    } yield ()
}
