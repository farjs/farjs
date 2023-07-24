package farjs.ui.popup

import scala.scalajs.js

sealed trait StatusPopupProps extends js.Object {
  val text: String
  val title: js.UndefOr[String]
  val onClose: js.UndefOr[js.Function0[Unit]]
}

object StatusPopupProps {

  def apply(text: String,
            title: js.UndefOr[String] = js.undefined,
            onClose: js.UndefOr[js.Function0[Unit]] = js.undefined
           ): StatusPopupProps = {

    js.Dynamic.literal(
      text = text,
      title = title,
      onClose = onClose
    ).asInstanceOf[StatusPopupProps]
  }

  def unapply(arg: StatusPopupProps): Option[
    (String, js.UndefOr[String], js.UndefOr[js.Function0[Unit]])
  ] = {
    Some((
      arg.text,
      arg.title,
      arg.onClose
    ))
  }
}
