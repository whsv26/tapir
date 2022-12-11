package org.whsv26.tapir
package modules.orders

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

import java.util.UUID

case class Order(
  id: Order.Id,
  products: List[OrderedProduct]
)

object Order {
  implicit val encoder: Encoder[Order] = deriveEncoder[Order]
  implicit val decoder: Decoder[Order] = deriveDecoder[Order]

  @newtype case class Id(value: UUID)

  object Id {
    def next: Id = Id(UUID.randomUUID())

    implicit val decoder: Decoder[Id] = deriving
    implicit val encoder: Encoder[Id] = deriving
  }
}
