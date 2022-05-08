package org.whsv26.tapir
package domain

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import sttp.tapir.{Codec, CodecFormat, Schema}

import java.util.UUID

package object foos {
  @newtype case class FooId(value: UUID)

  object FooId {
    def next: FooId = FooId(UUID.randomUUID())

    implicit val decoder: Decoder[FooId] = deriving
    implicit val encoder: Encoder[FooId] = deriving
    implicit val schema: Schema[FooId] = deriving
    implicit val codec: Codec[String, FooId, CodecFormat.TextPlain] =
      Codec.uuid.map[FooId](FooId(_))(_.value)
  }
}
