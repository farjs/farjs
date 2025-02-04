package farjs.file.popups

import scala.scalajs.js

sealed trait EncodingsPopupProps extends js.Object {
  val encoding: String
  val onApply: js.Function1[String, Unit]
  val onClose: js.Function0[Unit]
}

object EncodingsPopupProps {

  def apply(encoding: String,
            onApply: js.Function1[String, Unit],
            onClose: js.Function0[Unit]): EncodingsPopupProps = {

    js.Dynamic.literal(
      encoding = encoding,
      onApply = onApply,
      onClose = onClose
    ).asInstanceOf[EncodingsPopupProps]
  }

  def unapply(arg: EncodingsPopupProps): Option[(String, js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.encoding,
      arg.onApply,
      arg.onClose
    ))
  }
}
