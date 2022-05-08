package org.whsv26.tapir
package domain.auth

import cats.effect.kernel.Sync

import java.time.Clock

trait JwtClockAlg[F[_]] {
  def utc: F[Clock]
}

object JwtClockAlg {
  def apply[F[_]: Sync]: JwtClockAlg[F] = new JwtClockAlg[F] {
    override def utc: F[Clock] =
      Sync[F].delay(Clock.systemUTC())
  }
}
