package farjs.ui.task

import scala.scalajs.js

sealed trait TaskManagerProps extends js.Object {
  val startTask: js.UndefOr[Task]
}

object TaskManagerProps {

  def apply(startTask: js.UndefOr[Task]): TaskManagerProps = {
    js.Dynamic.literal(
      startTask = startTask
    ).asInstanceOf[TaskManagerProps]
  }

  def unapply(arg: TaskManagerProps): Option[js.UndefOr[Task]] = {
    Some(
      arg.startTask
    )
  }
}
