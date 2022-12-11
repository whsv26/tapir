package org.whsv26.tapir
package modules.foos.delete

import modules.foos.{DeleteFooCommand, FooService}
import util.bus.CommandHandler
import cats.effect.kernel.MonadCancelThrow

class DeleteFooHandler[F[_]: MonadCancelThrow](
  foos: FooService[F]
) extends CommandHandler[F, DeleteFooCommand] {
  override def handle = { command =>
    foos
      .delete(command.id)
      .value
  }
}
