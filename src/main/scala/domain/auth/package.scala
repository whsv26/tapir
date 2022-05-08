package org.whsv26.tapir
package domain

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros._
import sttp.tapir.{Codec, CodecFormat, Schema}

package object auth {
  @newtype case class JwtToken(value: String)

  object JwtToken {
    implicit val decoder: Decoder[JwtToken] = deriving
    implicit val encoder: Encoder[JwtToken] = deriving
    implicit val schema: Schema[JwtToken] = deriving
    implicit val codec: Codec[String, JwtToken, CodecFormat.TextPlain] =
      Codec.string.map[JwtToken](JwtToken(_))(_.value)
  }
}
