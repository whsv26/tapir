package org.whsv26.tapir
package domain.auth

import cats.effect.kernel.{Resource, Sync}

import java.time.Clock

trait JwtClockAlg[F[_]] {
  def utc: F[Clock]
}

object JwtClockAlg {
  def apply[F[_]: Sync]: Resource[F, JwtClockAlg[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new JwtClockAlg[F] {
        override def utc: F[Clock] =
          Sync[F].delay(Clock.systemUTC())
      }
    }
}
