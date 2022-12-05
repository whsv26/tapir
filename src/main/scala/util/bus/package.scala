package org.whsv26.tapir
package util

import cats.effect.implicits.genSpawnOps
import cats.effect.kernel.Async
import cats.syntax.functor._
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}

import scala.reflect.ClassTag

package object bus {
  trait Notification

  trait Request {
    type Out
  }

  object Request {
    type Aux[T] = Request { type Out = T }
  }

  trait QueryRequest extends Request

  trait Query[T] extends QueryRequest {
    override type Out = T
  }

  trait CommandRequest extends Request

  trait Command[T] extends CommandRequest {
    override type Out = T
  }

  trait NotificationHandlerBase[F[_]] {
    type In <: Notification

    implicit def tag: ClassTag[In]

    def handle: In => F[Unit]

    final def handler: Pipe[F, Notification, Unit] =
      _.collect { case n: In => n }.evalMap(handle)
  }

  abstract class NotificationHandler[F[_], N <: Notification](implicit
    notificationTag: ClassTag[N]
  ) extends NotificationHandlerBase[F] {
    override type In = N
    override implicit def tag: ClassTag[In] = notificationTag
  }

  trait RequestHandlerBase[F[_]] {
    type In <: Request
    type Out = In#Out
    def tag: ClassTag[In]
    def handle: In => F[Out]
  }

  trait RequestHandler[F[_], R <: Request] extends RequestHandlerBase[F] {
    override type In = R
  }

  abstract class QueryHandler[F[_], Q <: QueryRequest](implicit
    queryTag: ClassTag[Q]
  ) extends RequestHandler[F, Q] {
    override def tag: ClassTag[Q] = queryTag
  }

  abstract class CommandHandler[F[_], C <: CommandRequest](implicit
    commandTag: ClassTag[C]
  ) extends RequestHandler[F, C] {
    override def tag: ClassTag[C] = commandTag
  }

  trait Mediator[F[_]] {
    def send[Out](request: Request.Aux[Out]): F[Out]
    def publish(notification: Notification): F[Unit]
  }

  object Mediator {
    class Impl[F[_]: Async](
      notificationTopic: Topic[F, Notification],
      notificationHandlers: List[NotificationHandlerBase[F]],
      requestHandlers: List[RequestHandlerBase[F]],
    ) extends Mediator[F] {

      private val requestHandlersMap = requestHandlers.map {
        handler => handler.tag.runtimeClass -> handler
      }.toMap

      override def send[Out](request: Request.Aux[Out]): F[Out] =
        requestHandlersMap(request.getClass)
          .asInstanceOf[RequestHandler[F, Request.Aux[Out]]]
          .handle(request)

      override def publish(notification: Notification): F[Unit] =
        notificationTopic.publish1(notification).void

      def start: F[Unit] =
        Stream
          .emits(notificationHandlers)
          .map(_.handler)
          .map { handler =>
            notificationTopic
              .subscribe(20)
              .through(handler)
          }
          .parJoinUnbounded
          .compile
          .drain
          .start
          .void
    }
  }
}
