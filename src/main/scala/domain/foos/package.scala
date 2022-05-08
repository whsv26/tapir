package org.whsv26.tapir
package domain

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema

import java.util.UUID

package object foos {
  @newtype case class FooId(value: UUID)

  object FooId {
    implicit val decoder: Decoder[FooId] = deriving
    implicit val encoder: Encoder[FooId] = deriving
    implicit val schema: Schema[FooId] = deriving
  }
}
