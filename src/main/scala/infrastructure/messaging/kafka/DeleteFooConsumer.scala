package org.whsv26.tapir
package infrastructure.messaging.kafka

import config.Config.AppConfig
import domain.foos.{FooId, FooService}

import cats.effect.kernel.{Async, Resource, Spawn, Sync}
import cats.implicits._
import fs2.Stream
import fs2.kafka._

import scala.concurrent.duration._

class DeleteFooConsumer[F[_]: Async](
  foos: FooService[F],
  conf: AppConfig
) {

  private val baseSettings = ConsumerSettings[F, FooId, FooId](
    Deserializer.uuid[F].map(FooId.apply),
    Deserializer.uuid[F].map(FooId.apply),
  )

  private val consumerSettings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers.value)
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withEnableAutoCommit(false)
    .withGroupId("foo-delete-group")
    .withPollInterval(1.second)

  def start: F[Unit] =
    KafkaConsumer
      .stream[F, FooId, FooId](consumerSettings)
      .subscribeTo("foo-delete-topic")
      .records
      .mapAsync(25) { committable =>
        foos
          .delete(committable.record.value)
          .value
          .as(committable.offset)
      }
      .through(commitBatchWithin(500, 10.seconds))
      .compile
      .drain
}

object DeleteFooConsumer {
  def start[F[_]: Async: Spawn](
    foos: FooService[F],
    conf: AppConfig
  ): Resource[F, Unit] =
    Resource.eval {
      Spawn[F].start {
        new DeleteFooConsumer[F](foos, conf).start
      }
    }.void

}
