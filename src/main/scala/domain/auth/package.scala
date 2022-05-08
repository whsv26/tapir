package org.whsv26.tapir
package domain

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros._
import sttp.tapir.{Codec, CodecFormat, Schema}

package object auth {
  @newtype case class Token(value: String)

  object Token {
    implicit val decoder: Decoder[Token] = deriving
    implicit val encoder: Encoder[Token] = deriving
    implicit val schema: Schema[Token] = deriving
    implicit val codec: Codec[String, Token, CodecFormat.TextPlain] =
      Codec.string.map[Token](Token(_))(_.value)
  }
}
