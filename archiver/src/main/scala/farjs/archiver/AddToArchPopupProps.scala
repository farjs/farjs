package farjs.archiver

import scala.scalajs.js

sealed trait AddToArchPopupProps extends js.Object {
  val zipName: String
  val action: AddToArchAction
  val onAction: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object AddToArchPopupProps {

  def apply(zipName: String,
            action: AddToArchAction,
            onAction: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): AddToArchPopupProps = {

    js.Dynamic.literal(
      zipName = zipName,
      action = action,
      onAction = onAction,
      onCancel = onCancel
    ).asInstanceOf[AddToArchPopupProps]
  }

  def unapply(arg: AddToArchPopupProps): Option[(String, AddToArchAction, js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.zipName,
      arg.action,
      arg.onAction,
      arg.onCancel
    ))
  }
}
