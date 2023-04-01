package farjs.ui.task

object TaskReducer {

  def apply(state: Option[AbstractTask], action: Any): Option[AbstractTask] = {
    action match {
      case a: TaskAction => Some(a.task)
      case _ => state
    }
  }
}
