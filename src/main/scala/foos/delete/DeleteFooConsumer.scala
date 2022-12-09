package org.whsv26.tapir
package foos.delete

import config.Config.AppConfig

import cats.effect.implicits.genSpawnOps
import cats.effect.kernel.{Async, Resource, Spawn}
import cats.implicits._
import fs2.kafka._
import org.whsv26.tapir.foos.{Foo, FooService}

import scala.concurrent.duration._

class DeleteFooConsumer[F[_]: Async](
  foos: FooService[F],
  conf: AppConfig
) {

  private val baseSettings = ConsumerSettings[F, Foo.Id, Foo.Id](
    Deserializer.uuid[F].map(Foo.Id.apply),
    Deserializer.uuid[F].map(Foo.Id.apply),
  )

  private val consumerSettings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers.value)
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withEnableAutoCommit(false)
    .withGroupId("foo-delete-group")
    .withPollInterval(1.second)

  def start: F[Unit] =
    KafkaConsumer
      .stream[F, Foo.Id, Foo.Id](consumerSettings)
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
      .start
      .void
}
