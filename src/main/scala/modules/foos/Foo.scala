package org.whsv26.tapir
package modules.foos

import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import sttp.tapir.{Codec, CodecFormat, Schema}
import doobie.util.Read
import util.doobie._

import java.util.UUID

final case class Foo(
  id: Foo.Id,
  a: NonNegInt,
  b: Boolean
)

object Foo {
  implicit val encoder: Encoder[Foo] = deriveEncoder[Foo]
  implicit val decoder: Decoder[Foo] = deriveDecoder[Foo]
  implicit val read: Read[Foo] = implicitly

  @newtype case class Id(value: UUID)

  object Id {
    def next: Id = Id(UUID.randomUUID())

    implicit val decoder: Decoder[Id] = deriving
    implicit val encoder: Encoder[Id] = deriving
    implicit val schema: Schema[Id] = deriving
    implicit val codec: Codec[String, Id, CodecFormat.TextPlain] =
      Codec.uuid.map[Id](Id(_))(_.value)
  }
}

