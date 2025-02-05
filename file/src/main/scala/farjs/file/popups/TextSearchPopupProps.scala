package farjs.file.popups

import scala.scalajs.js

sealed trait TextSearchPopupProps extends js.Object {
  val onSearch: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object TextSearchPopupProps {

  def apply(onSearch: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): TextSearchPopupProps = {

    js.Dynamic.literal(
      onSearch = onSearch,
      onCancel = onCancel
    ).asInstanceOf[TextSearchPopupProps]
  }

  def unapply(arg: TextSearchPopupProps): Option[(js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.onSearch,
      arg.onCancel
    ))
  }
}
