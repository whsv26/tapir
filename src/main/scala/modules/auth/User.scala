package org.whsv26.tapir
package modules.auth

import modules.auth.User.PasswordHash
import util.tapir._

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema

import java.util.UUID

case class User(
  id: User.Id,
  name: User.Name,
  email: User.Email,
  password: PasswordHash
)

object User {
  @newtype case class Id(value: UUID)

  @newtype case class Name(value: String)

  @newtype case class Email(value: String)

  @newtype case class PlainPassword(value: String)

  @newtype case class PasswordHash(value: String)

  @newtype case class Token(value: String)

  object Token {
    implicit val decoder: Decoder[Token] = deriving
    implicit val encoder: Encoder[Token] = deriving
    implicit val schema: Schema[Token] = deriving
    implicit val codec: ParamCodec[Token] = newTypeParamCodec[Token, String]
  }
}
