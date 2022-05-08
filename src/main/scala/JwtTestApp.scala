package org.whsv26.tapir

import config.Config.AppConfig

import cats.effect.std.Console
import cats.effect.{IO, IOApp, Sync}
import cats.implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import tsec.jws.signature.JWTSig
import tsec.jwt.JWTClaims
import tsec.signature.jca.{SHA256withECDSA, SigKeyPair}
import eu.timepit.refined.pureconfig._

import java.security.{PrivateKey, PublicKey}
import java.util.{Base64, UUID}

object JwtTestApp extends IOApp.Simple {
  private lazy val conf: AppConfig = ConfigSource
    .resources("config/app.conf")
    .loadOrThrow[AppConfig]

  private val b64enc = Base64.getEncoder
  private val b64dec = Base64.getDecoder

  def generateKeyPair[F[_]: Sync: Console]: F[SigKeyPair[SHA256withECDSA]] = for {
    keyPair <- SHA256withECDSA.generateKeyPair[F]
    pubKeyEncoded = keyPair.publicKey.asInstanceOf[PublicKey].getEncoded
    priKeyEncoded = keyPair.privateKey.asInstanceOf[PrivateKey].getEncoded

    _ <- Console[F].println("Public key: ") *>
      Console[F].println(b64enc.encodeToString(pubKeyEncoded)) *>
      Console[F].println("Private key: ") *>
      Console[F].println(b64enc.encodeToString(priKeyEncoded))
  } yield keyPair

  def buildJwtToken[F[_]: Sync](claims: JWTClaims): F[JWTSig[SHA256withECDSA]] = for {
    priKey <- SHA256withECDSA.buildPrivateKey[F](b64dec.decode(conf.jwt.privateKey))
    jwtSig <- JWTSig.signAndBuild[F, SHA256withECDSA](claims, priKey)
  } yield jwtSig

  def verifyJwtToken[F[_]: Sync: Console](token: String): F[JWTSig[SHA256withECDSA]] = for {
    pubKey <- SHA256withECDSA.buildPublicKey[F](b64dec.decode(conf.jwt.publicKey))
    verified <- JWTSig.verifyK[F, SHA256withECDSA](token, pubKey)
  } yield verified

  override def run: IO[Unit] = {
    val claims = JWTClaims(subject = UUID.randomUUID().toString.some)

    for {
      token <- buildJwtToken[IO](claims)
      verified <- verifyJwtToken[IO](token.toEncodedString)
      _ <- IO.println(verified.body.subject)
    } yield verified
  }
}
