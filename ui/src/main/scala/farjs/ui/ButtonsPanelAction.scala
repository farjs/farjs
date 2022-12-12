package farjs.ui

import scala.scalajs.js

sealed trait ButtonsPanelAction extends js.Object {
  val label: String
  val onAction: js.Function0[Unit]
}

object ButtonsPanelAction {

  def apply(label: String, onAction: js.Function0[Unit]): ButtonsPanelAction = {
    js.Dynamic.literal(
      label = label,
      onAction = onAction
    ).asInstanceOf[ButtonsPanelAction]
  }
}
