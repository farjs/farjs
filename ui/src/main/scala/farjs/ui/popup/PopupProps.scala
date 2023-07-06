package farjs.ui.popup

import scala.scalajs.js

sealed trait PopupProps extends js.Object {
  val onClose: js.UndefOr[js.Function0[Unit]]
  val focusable: js.UndefOr[Boolean]
  val onOpen: js.UndefOr[js.Function0[Unit]]
  val onKeypress: js.UndefOr[js.Function1[String, Boolean]]
}

object PopupProps {

  def apply(onClose: js.UndefOr[js.Function0[Unit]] = js.undefined,
            focusable: js.UndefOr[Boolean] = js.undefined,
            onOpen: js.UndefOr[js.Function0[Unit]] = js.undefined,
            onKeypress: js.UndefOr[js.Function1[String, Boolean]] = js.undefined): PopupProps = {

    js.Dynamic.literal(
      onClose = onClose,
      focusable = focusable,
      onOpen = onOpen,
      onKeypress = onKeypress
    ).asInstanceOf[PopupProps]
  }

  def unapply(arg: PopupProps): Option[
    (js.UndefOr[js.Function0[Unit]], js.UndefOr[Boolean], js.UndefOr[js.Function0[Unit]], js.UndefOr[js.Function1[String, Boolean]])
  ] = {
    Some((
      arg.onClose,
      arg.focusable,
      arg.onOpen,
      arg.onKeypress
    ))
  }

  def copy(p: PopupProps)(onClose: js.UndefOr[js.Function0[Unit]] = p.onClose,
                          focusable: js.UndefOr[Boolean] = p.focusable,
                          onOpen: js.UndefOr[js.Function0[Unit]] = p.onOpen,
                          onKeypress: js.UndefOr[js.Function1[String, Boolean]] = p.onKeypress): PopupProps = {
    PopupProps(
      onClose = onClose,
      focusable = focusable,
      onOpen = onOpen,
      onKeypress = onKeypress
    )
  }
}
