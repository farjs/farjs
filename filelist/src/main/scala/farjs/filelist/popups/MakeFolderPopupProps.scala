package farjs.filelist.popups

import scala.scalajs.js

sealed trait MakeFolderPopupProps extends js.Object {
  val multiple: Boolean
  val onOk: js.Function2[String, Boolean, Unit]
  val onCancel: js.Function0[Unit]
}

object MakeFolderPopupProps {

  def apply(multiple: Boolean,
            onOk: js.Function2[String, Boolean, Unit],
            onCancel: js.Function0[Unit]): MakeFolderPopupProps = {
    js.Dynamic.literal(
      multiple = multiple,
      onOk = onOk,
      onCancel = onCancel
    ).asInstanceOf[MakeFolderPopupProps]
  }

  def unapply(arg: MakeFolderPopupProps): Option[(Boolean, js.Function2[String, Boolean, Unit], js.Function0[Unit])] = {
    Some((
      arg.multiple,
      arg.onOk,
      arg.onCancel
    ))
  }
}
