package org.whsv26.tapir
package infrastructure.messaging.kafka

import config.Config.AppConfig
import domain.foos.Foo.FooId

import cats.effect.kernel.Async
import cats.implicits._
import fs2.kafka._

class DeleteFooProducer[F[_]: Async](conf: AppConfig) {

  private val baseSettings = ProducerSettings(
    keySerializer = Serializer[F, FooId],
    valueSerializer = Serializer[F, FooId]
  )

  private val settings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers)
    .withAcks(Acks.All)

  def produce(id: FooId): F[Unit] =
    KafkaProducer.resource(settings)
      .use { producer =>
        val record = ProducerRecord("foo-delete-topic", id, id)
        producer.produce(ProducerRecords.one(record))
      }
      .flatten
      .void
}
