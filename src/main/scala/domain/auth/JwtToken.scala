package org.whsv26.tapir
package domain.auth

import sttp.tapir.{Codec, CodecFormat}

case class JwtToken(value: String)

object JwtToken {
  implicit val codec: Codec[String, JwtToken, CodecFormat.TextPlain] =
    Codec.string.map[JwtToken](JwtToken(_))(_.value)
}


