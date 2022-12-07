package org.whsv26.tapir

import util.bus._

import cats.effect.IO


object TestShapeless extends App {
  case class MyQuery1(i: Int) extends Query[Boolean]

  class MyHandler1 extends QueryHandler[IO, MyQuery1] {
    override def handle =
      query => IO(query.i > 0)
  }

  case class MyCommand1(i: Int) extends Command[String]

  class MyHandler2 extends CommandHandler[IO, MyCommand1] {
    override def handle =
      query => IO(Option.when(query.i > 0)("gt 0").getOrElse("lte 0"))
  }

  case class MyNotification1(i: Int) extends Notification

  class MyHandler3 extends NotificationHandler[IO, MyNotification1] {
    override def handle =
      query => IO(Option.when(query.i > 0)("gt 0").getOrElse("lte 0"))
  }

  case class Deps(h1: MyHandler1, h2: MyHandler2, h3: MyHandler3)
  val deps = Deps(new MyHandler1, new MyHandler2, new MyHandler3)

  val genericDeps = shapeless.Generic[Deps].to(deps)
  val requestHandlers = genericDeps.unifySubtypes[RequestHandlerBase[IO]].filter[RequestHandlerBase[IO]].to[List]
  val notificationHandlers = genericDeps.unifySubtypes[NotificationHandlerBase[IO]].filter[NotificationHandlerBase[IO]].to[List]

  println(requestHandlers)
  println(notificationHandlers)

}
