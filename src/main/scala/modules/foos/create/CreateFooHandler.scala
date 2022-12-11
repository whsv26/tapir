package org.whsv26.tapir
package modules.foos.create

import modules.foos.{CreateFooCommand, Foo, FooService}
import util.bus.CommandHandler

import cats.effect.kernel.MonadCancelThrow

class CreateFooHandler[F[_]: MonadCancelThrow](
  foos: FooService[F]
) extends CommandHandler[F, CreateFooCommand] {

  override def handle = { c =>
    foos
      .create(Foo(c.id, c.a, c.b))
      .value
  }
}
