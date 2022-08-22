package farjs.ui.popup

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

sealed trait MessageBoxAction extends js.Object {
  val label: String
  val onAction: js.Function0[Unit]
  val triggeredOnClose: Boolean
}

@JSExportAll
sealed trait MessageBoxActionExports {

  def OK(onAction: js.Function0[Unit]): MessageBoxAction =
    MessageBoxAction("OK", onAction, triggeredOnClose = true)
  
  def YES(onAction: js.Function0[Unit]): MessageBoxAction =
    MessageBoxAction("YES", onAction)
  
  def NO(onAction: js.Function0[Unit]): MessageBoxAction =
    MessageBoxAction("NO", onAction, triggeredOnClose = true)

  def NO_NON_CLOSABLE(onAction: js.Function0[Unit]): MessageBoxAction =
    MessageBoxAction("NO", onAction)
}

object MessageBoxAction extends MessageBoxActionExports {

  def apply(label: String,
            onAction: js.Function0[Unit],
            triggeredOnClose: Boolean = false): MessageBoxAction = {

    js.Dynamic.literal(
      label = label,
      onAction = onAction,
      triggeredOnClose = triggeredOnClose
    ).asInstanceOf[MessageBoxAction]
  }

  def unapply(arg: MessageBoxAction): Option[(String, js.Function0[Unit], Boolean)] = {
    Some((
      arg.label,
      arg.onAction,
      arg.triggeredOnClose
    ))
  }
}
