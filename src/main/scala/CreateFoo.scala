package org.whsv26.tapir

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateFoo(a: Int, b: Boolean)

object CreateFoo {
  implicit val encoder: Encoder[CreateFoo] = deriveEncoder[CreateFoo]
  implicit val decoder: Decoder[CreateFoo] = deriveDecoder[CreateFoo]
}



