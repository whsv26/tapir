package org.whsv26.tapir
package modules.orders

import doobie.util.Read
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema

import java.util.UUID

case class Product(
  id: Product.Id,
  name: String,
  price: BigDecimal
)

object Product {
  implicit val encoder: Encoder[Product] = deriveEncoder[Product]
  implicit val decoder: Decoder[Product] = deriveDecoder[Product]

  @newtype case class Id(value: UUID)

  object Id {
    def next: Id = Id(UUID.randomUUID())

    implicit val decoder: Decoder[Id] = deriving
    implicit val encoder: Encoder[Id] = deriving
  }
}
