package org.whsv26.tapir
package domain.foos

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class Foo(id: FooId, a: Int, b: Boolean)

object Foo {
  implicit val encoder: Encoder[Foo] = deriveEncoder[Foo]
  implicit val decoder: Decoder[Foo] = deriveDecoder[Foo]
}

