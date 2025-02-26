package farjs.viewer

import scommons.react.raw.NativeRef

import scala.scalajs.js

sealed trait ViewerInputProps extends js.Object {
  val inputRef: NativeRef
  val onWheel: js.Function1[Boolean, Unit]
  val onKeypress: js.Function1[String, Unit]
}

object ViewerInputProps {

  def apply(inputRef: NativeRef,
            onWheel: js.Function1[Boolean, Unit] = _ => (),
            onKeypress: js.Function1[String, Unit] = _ => ()): ViewerInputProps = {

    js.Dynamic.literal(
      inputRef = inputRef,
      onWheel = onWheel,
      onKeypress = onKeypress
    ).asInstanceOf[ViewerInputProps]
  }

  def unapply(arg: ViewerInputProps): Option[(NativeRef, js.Function1[Boolean, Unit], js.Function1[String, Unit])] = {
    Some((
      arg.inputRef,
      arg.onWheel,
      arg.onKeypress
    ))
  }
}
