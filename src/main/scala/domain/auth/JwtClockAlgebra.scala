package org.whsv26.tapir
package domain.auth

import cats.effect.kernel.Sync

import java.time.Clock

trait JwtClockAlgebra[F[_]] {
  def utc: F[Clock]
}

object JwtClockAlgebra {
  def apply[F[_]: Sync]: JwtClockAlgebra[F] = new JwtClockAlgebra[F] {
    override def utc: F[Clock] =
      Sync[F].delay(Clock.systemUTC())
  }
}
