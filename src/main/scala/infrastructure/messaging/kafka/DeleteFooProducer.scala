package org.whsv26.tapir
package infrastructure.messaging.kafka

import config.Config.AppConfig
import domain.foos.FooId

import cats.effect.kernel.{Async, Resource, Sync}
import cats.implicits._
import fs2.kafka._

import java.util.UUID

class DeleteFooProducer[F[_]: Async](conf: AppConfig) {

  private val baseSettings = ProducerSettings(
    keySerializer = Serializer[F, UUID].contramap[FooId](_.value),
    valueSerializer = Serializer[F, UUID].contramap[FooId](_.value),
  )

  private val settings = baseSettings
    .withBootstrapServers(conf.kafka.bootstrapServers.value)
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

object DeleteFooProducer {
  def apply[F[_]: Async](
    conf: AppConfig
  ): Resource[F, DeleteFooProducer[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new DeleteFooProducer[F](conf)
    }
}
