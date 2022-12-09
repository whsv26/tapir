package org.whsv26.tapir
package modules.auth

import User.PasswordHash

import io.estatico.newtype.macros.newtype
import io.circe.{Decoder, Encoder}
import sttp.tapir.{Codec, CodecFormat, Schema}


import java.util.UUID

case class User(
  id: User.Id,
  name: User.Name,
  password: PasswordHash
)

object User {
  @newtype case class Id(value: UUID)

  @newtype case class Name(value: String)

  @newtype case class PlainPassword(value: String)

  @newtype case class PasswordHash(value: String)

  @newtype case class Token(value: String)

  object Token {
    implicit val decoder: Decoder[Token] = deriving
    implicit val encoder: Encoder[Token] = deriving
    implicit val schema: Schema[Token] = deriving
    implicit val codec: Codec[String, Token, CodecFormat.TextPlain] =
      Codec.string.map[Token](Token(_))(_.value)
  }
}
