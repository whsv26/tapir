package org.whsv26.tapir
package util.security

import cats.Monad
import cats.effect.std.Console
import cats.implicits._
import tsec.common._

import java.security.{Key, PrivateKey, PublicKey}

class Pem {
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
}
