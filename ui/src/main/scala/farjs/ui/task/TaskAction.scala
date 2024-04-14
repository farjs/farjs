package farjs.ui.task

import scala.scalajs.js

sealed trait TaskAction extends js.Object {

  val task: Task
}

object TaskAction {

  def apply(task: Task): TaskAction = {
    js.Dynamic.literal(
      task = task
    ).asInstanceOf[TaskAction]
  }

  def unapply(arg: TaskAction): Option[Task] =
    Some(arg.task)
}
