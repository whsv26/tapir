package org.whsv26.tapir

import Config.AppConfig
import Foo.FooId
import cats.effect.kernel.Async
import cats.implicits._
import fs2.kafka._
import fs2.Stream
import scala.concurrent.duration._

class DeleteFooConsumer[F[_]: Async](
  foos: FooService[F],
  conf: AppConfig
) {

  private val baseSettings = ConsumerSettings[F, FooId, FooId](
    Deserializer.uuid[F],
    Deserializer.uuid[F],
  )

  private val consumerSettings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers)
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withEnableAutoCommit(false)
    .withGroupId("foo-delete-group")

  def stream: Stream[F, Unit] = {
    KafkaConsumer
      .stream[F, FooId, FooId](consumerSettings)
      .subscribeTo("foo-delete-topic")
      .records
      .mapAsync(25) { committable =>
        foos
          .delete(committable.record.value)
          .as(committable.offset)
      }
      .through(commitBatchWithin(500, 10.seconds))
  }
}
