package org.whsv26.tapir
package util

import cats.effect.kernel.Async
import cats.syntax.functor._
import cats.syntax.traverse._

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
  }

  abstract class NotificationHandler[F[_], N <: Notification](implicit
    notificationTag: ClassTag[N]
  ) extends NotificationHandlerBase[F] {
    override type In = N
    override implicit def tag: ClassTag[In] = notificationTag
  }

  trait RequestHandlerBase[F[_]] {
    type In <: Request
    final type Out = In#Out
    def tag: ClassTag[In]
    def handle: In => F[Out]
  }

  trait RequestHandler[F[_], R <: Request] extends RequestHandlerBase[F] {
    override final type In = R
  }

  abstract class QueryHandler[F[_], Q <: QueryRequest](implicit
    queryTag: ClassTag[Q]
  ) extends RequestHandler[F, Q] {
    override final def tag: ClassTag[Q] = queryTag
  }

  abstract class CommandHandler[F[_], C <: CommandRequest](implicit
    commandTag: ClassTag[C]
  ) extends RequestHandler[F, C] {
    override final def tag: ClassTag[C] = commandTag
  }

  trait Mediator[F[_]] {
    def send[Out](request: Request.Aux[Out]): F[Out]
    def publish(notification: Notification): F[Unit]
  }

  object Mediator {
    class Impl[F[_]: Async](
      requestHandlers: Set[RequestHandlerBase[F]],
      notificationHandlers: Set[NotificationHandlerBase[F]],
    ) extends Mediator[F] {

      private val requestHandlersMap = requestHandlers.map {
        handler => handler.tag.runtimeClass -> handler
      }.toMap

      private val notificationHandlersMap = notificationHandlers
        .toList
        .groupBy(_.tag.runtimeClass)

      override def send[Out](request: Request.Aux[Out]): F[Out] =
        requestHandlersMap(request.getClass)
          .asInstanceOf[RequestHandler[F, Request.Aux[Out]]]
          .handle(request)

      override def publish(notification: Notification): F[Unit] =
        notificationHandlersMap(notification.getClass)
          .traverse { handler =>
            handler
              .asInstanceOf[NotificationHandler[F, Notification]]
              .handle(notification)
          }
          .void
    }
  }
}
