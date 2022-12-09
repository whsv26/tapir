package org.whsv26.tapir

import util.bus._

import cats.effect.{IO, IOApp}
import cats.syntax.traverse._
import fs2.concurrent.Topic

import scala.concurrent.duration.DurationInt


object MediatorTestApp extends IOApp.Simple {
  case class MyQuery1(i: Int) extends Query[Boolean]

  class MyHandler1 extends QueryHandler[IO, MyQuery1] {
    override def handle =
      query => IO(query.i > 0)
  }

  case class MyQuery2(i: Int) extends Query[String]

  class MyHandler2 extends QueryHandler[IO, MyQuery2] {
    override def handle =
      query => IO(Option.when(query.i > 0)("gt 0").getOrElse("lte 0"))
  }

  case class SomethingHappened(message: String) extends Notification

  case class SomethingElseHappened(message: String) extends Notification

  class SomethingHappenedHandler1 extends NotificationHandler[IO, SomethingHappened] {
    override def handle: SomethingHappened => IO[Unit] = event => IO.println("1: " + event.message)
  }

  class SomethingHappenedHandler2 extends NotificationHandler[IO, SomethingHappened] {
    override def handle: SomethingHappened => IO[Unit] = event => IO.println("2: " + event.message)
  }

  class SomethingElseHappenedHandler extends NotificationHandler[IO, SomethingElseHappened] {
    override def handle: SomethingElseHappened => IO[Unit] = event => IO.println("else: " + event.message)
  }

  override def run: IO[Unit] =
    for {
      topic <- Topic[IO, Notification]

      mediator = new Mediator.Impl[IO](
        Set(new MyHandler1, new MyHandler2),
        Set(new SomethingHappenedHandler1, new SomethingHappenedHandler2, new SomethingElseHappenedHandler),
      )

      _ <- List(mediator.send(MyQuery2(1)), mediator.send(MyQuery1(2)).map(_.toString))
        .sequence
        .flatMap(IO.println)
        .void

      _ <- IO.sleep(1.seconds)
      _ <- mediator.publish(SomethingHappened("event happened!"))
      _ <- mediator.publish(SomethingElseHappened("event else happened..."))
      _ <- IO.sleep(1.seconds)

    } yield ()
}
