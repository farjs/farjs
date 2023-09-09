package farjs.ui.task

object TaskReducer {

  def apply(state: Option[Task], action: Any): Option[Task] = {
    action match {
      case a: TaskAction => Some(a.task)
      case _ => state
    }
  }
}
