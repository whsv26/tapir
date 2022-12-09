package org.whsv26.tapir
package foos.delete

import foos.DeleteFooCommand
import util.bus.CommandHandler

import cats.effect.kernel.MonadCancelThrow

class AsyncDeleteFooHandler[F[_]: MonadCancelThrow](
  producer: DeleteFooProducer[F],
) extends CommandHandler[F, DeleteFooCommand] {
  override def handle = { command =>
    producer.produce(command.id)
  }
}

