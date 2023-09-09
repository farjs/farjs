package farjs.ui.task

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

sealed trait Task extends js.Object {
  val startTime: Double
  val message: String
  val result: js.Promise[Any]
}

object Task {

  def apply(message: String, result: Future[_]): Task = {
    create(message, result.toJSPromise)
  }
  
  def create(message: String, result: js.Promise[Any], startTime: Double = js.Date.now()): Task = {
    js.Dynamic.literal(
      startTime = startTime,
      message = message,
      result = result
    ).asInstanceOf[Task]
  }

  def unapply(arg: Task): Option[(String, Future[_])] = {
    Some((
      arg.message,
      arg.result.toFuture
    ))
  }
}
