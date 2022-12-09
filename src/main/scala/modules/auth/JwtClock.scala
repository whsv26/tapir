package org.whsv26.tapir
package modules.auth

import cats.effect.kernel.Sync

import java.time.Clock

trait JwtClock[F[_]] {
  def utc: F[Clock]
}

object JwtClock {
  class SystemImpl[F[_]: Sync] extends JwtClock[F] {
    override def utc: F[Clock] =
      Sync[F].delay(Clock.systemUTC())
  }
}
