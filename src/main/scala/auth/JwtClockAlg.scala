package org.whsv26.tapir
package auth

import cats.effect.kernel.{Resource, Sync}

import java.time.Clock

trait JwtClockAlg[F[_]] {
  def utc: F[Clock]
}

object JwtClockAlg {
  class SystemClockImpl[F[_]: Sync] extends JwtClockAlg[F] {
    override def utc: F[Clock] =
      Sync[F].delay(Clock.systemUTC())
  }
}
