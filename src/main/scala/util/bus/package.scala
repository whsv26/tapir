package org.whsv26.tapir
package util

import cats.Id
import cats.effect.kernel.{Async, Sync}
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import org.whsv26.tapir.util.bus.Request.Aux
import cats.syntax.functor._
import cats.syntax.traverse._

import scala.reflect.ClassTag

package object bus {

  trait Mediator[F[_]] {
    def send[Out, Req <: Request.Aux[Out]](request: Req): F[Out]
    def publish(notification: Notification): F[Unit]
  }

  object Mediator {
    class Impl[F[_]: Async](
      requestHandlers: List[RequestHandlerBase[F]],
      notificationHandlers: List[NotificationHandlerBase[F]],
      notificationTopic: Topic[F, Notification]
    ) extends Mediator[F] {

      override def send[Out, Req <: Request.Aux[Out]](request: Req): F[Out] =
        requestHandlers.collectFirst {
          // TODO: Is it working? Why no type tag needed?
          // TODO: Optimize O(n) search
          case handler: RequestHandler[F, Req] => handler.handle(request)
        }.head

      override def publish(notification: Notification): F[Unit] =
        notificationTopic.publish1(notification).void

      def start: F[Unit] =
        Stream
          .emits(notificationHandlers)
          .map(_.handler)
          .map(handler => notificationTopic.subscribe(20).through(handler))
          .parJoinUnbounded
          .compile
          .drain
    }
  }

  trait Notification

  trait Request {
    type Out
  }

  object Request {
    type Aux[T] = Request { type Out = T }
  }

  trait QueryRequest extends Request

  trait CommandRequest extends Request

  trait Query[T] extends QueryRequest {
    override type Out = T
  }

  trait Command[T] extends CommandRequest {
    override type Out = T
  }

  trait NotificationHandlerBase[F[_]] {
    type In <: Request

    def handle: In => F[Unit]

    final def handler: Pipe[F, Notification, Unit] =
      _.collect { case n: In => n }.evalMap(handle)
  }

  trait RequestHandlerBase[F[_]] {
    type In <: Request
    type Out = In#Out
    def handle: In => F[Out]
  }

  trait RequestHandler[F[_], Req <: Request] extends RequestHandlerBase[F] {
    override type In = Req
  }

  trait QueryHandler[F[_], In <: QueryRequest] extends RequestHandler[F, In]

  trait CommandHandler[F[_], In <: CommandRequest] extends RequestHandler[F, In]

  case class MyQuery(i: Int) extends Query[Boolean]

  class MyHandler extends QueryHandler[Id, MyQuery] {
    override def handle =
      query => query.i > 0
  }
}
