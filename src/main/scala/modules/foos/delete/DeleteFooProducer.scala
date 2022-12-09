package org.whsv26.tapir
package modules.foos.delete

import config.Config.AppConfig
import modules.foos.Foo

import cats.effect.kernel.Async
import cats.implicits._
import fs2.kafka._

import java.util.UUID

class DeleteFooProducer[F[_]: Async](conf: AppConfig) {

  private val baseSettings = ProducerSettings(
    keySerializer = Serializer[F, UUID].contramap[Foo.Id](_.value),
    valueSerializer = Serializer[F, UUID].contramap[Foo.Id](_.value),
  )

  private val settings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers.value)
    .withAcks(Acks.All)

  def produce(id: Foo.Id): F[Unit] =
    KafkaProducer.resource(settings)
      .use { producer =>
        val record = ProducerRecord("foo-delete-topic", id, id)
        producer.produce(ProducerRecords.one(record))
      }
      .flatten
      .void
}
