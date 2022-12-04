package org.whsv26.tapir
package foos.delete

import config.Config.AppConfig

import cats.effect.kernel.{Async, Resource, Sync}
import cats.implicits._
import fs2.kafka._
import org.whsv26.tapir.foos.Foo

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

object DeleteFooProducer {
  def apply[F[_]: Async](
    conf: AppConfig
  ): Resource[F, DeleteFooProducer[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new DeleteFooProducer[F](conf)
    }
}
