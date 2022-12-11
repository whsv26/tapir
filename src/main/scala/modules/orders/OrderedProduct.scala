package org.whsv26.tapir
package modules.orders

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class OrderedProduct(
  product: Product,
  qty: Int
)

object OrderedProduct {
  implicit val encoder: Encoder[OrderedProduct] = deriveEncoder[OrderedProduct]
  implicit val decoder: Decoder[OrderedProduct] = deriveDecoder[OrderedProduct]
}
