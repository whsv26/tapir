package org.whsv26.tapir
package foos.delete

import foos.{DeleteFooCommand, FooService}
import util.bus.CommandHandler
import cats.effect.kernel.MonadCancelThrow

class DeleteFooHandler[F[_]: MonadCancelThrow](
  fooService: FooService[F]
) extends CommandHandler[F, DeleteFooCommand] {
  override def handle = { command =>
    fooService
      .delete(command.id)
      .value
  }
}