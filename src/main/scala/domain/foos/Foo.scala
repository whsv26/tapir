package org.whsv26.tapir
package domain.foos

import domain.foos.Foo.FooId

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID

final case class Foo(id: FooId, a: Int, b: Boolean)

object Foo {
  type FooId = UUID

  implicit val encoder: Encoder[Foo] = deriveEncoder[Foo]
  implicit val decoder: Decoder[Foo] = deriveDecoder[Foo]
}

