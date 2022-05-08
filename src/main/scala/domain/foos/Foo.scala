package org.whsv26.tapir
package domain.foos

import eu.timepit.refined.types.numeric.NonNegInt
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.refined._

final case class Foo(
  id: FooId,
  a: NonNegInt,
  b: Boolean
)

object Foo {
  implicit val encoder: Encoder[Foo] = deriveEncoder[Foo]
  implicit val decoder: Decoder[Foo] = deriveDecoder[Foo]
}

