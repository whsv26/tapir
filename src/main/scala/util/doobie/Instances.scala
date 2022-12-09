package org.whsv26.tapir
package util.doobie

import cats.Show
import doobie.util.Get
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV
import io.estatico.newtype.Coercible

import java.util.UUID

trait Instances {
  implicit val getUUID: Get[UUID] = Get[String].map(UUID.fromString)

  implicit def getForNewType[A: Get, B](implicit C: Coercible[A, B]): Get[B] =
    Get[A].map(C.apply)

  implicit def getForRefined[T: Show: Get, P](implicit V: Validate[T, P]): Get[Refined[T, P]] =
    Get[T].temap(t => refineV[P](t))
}
