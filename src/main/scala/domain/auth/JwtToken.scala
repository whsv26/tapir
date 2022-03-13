package org.whsv26.tapir
package domain.auth

import sttp.tapir.{Codec, CodecFormat, DecodeResult}

case class JwtToken(value: String)

object JwtToken {
  implicit val codec: Codec[String, JwtToken, CodecFormat.TextPlain] =
    Codec.string.mapDecode[JwtToken](t => DecodeResult.Value(JwtToken(t)))(_.value)
}


