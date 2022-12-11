package org.whsv26.tapir
package util.time

import cats.Functor
import cats.effect.kernel.Sync
import cats.syntax.functor._

import java.time.{Clock => JClock, Instant, ZoneId}

abstract class Clock[F[_]: Functor] {
  protected def clock: F[JClock]
  final def now: F[Instant] = clock.map(_.instant)
}

object Clock {
  class SystemImpl[F[_]: Sync] extends Clock[F] {
    override def clock: F[JClock] =
      Sync[F].delay(JClock.systemUTC())
  }

  class FixedImpl[F[_]: Sync](at: Instant) extends Clock[F] {
    override def clock: F[JClock] =
      Sync[F].delay(JClock.fixed(at, ZoneId.of("UTC")))
  }

  def fixed[F[_]: Sync](at: Instant) = new FixedImpl[F](at)
}
