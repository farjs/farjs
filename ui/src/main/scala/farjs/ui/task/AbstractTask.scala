package farjs.ui.task

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Try

trait AbstractTask {

  val startTime: Long = System.currentTimeMillis()

  def message: String

  def onComplete(f: Try[_] => Unit): Unit
}

case class FutureTask[T](message: String, future: Future[T]) extends AbstractTask {

  def onComplete(f: (Try[_]) => Unit): Unit = future.onComplete(f)
}
