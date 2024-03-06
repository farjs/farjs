package farjs.ui

import scommons.react.raw.NativeRef

import scala.scalajs.js

sealed trait TextInputProps extends js.Object {
  val inputRef: NativeRef
  val left: Int
  val top: Int
  val width: Int
  val value: String
  val state: TextInputState
  val stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit]
  val onChange: js.Function1[String, Unit]
  val onEnter: js.UndefOr[js.Function0[Unit]]
  val onKeypress: js.UndefOr[js.Function1[String, Boolean]]
}

object TextInputProps {

  def apply(inputRef: NativeRef,
            left: Int,
            top: Int,
            width: Int,
            value: String,
            state: TextInputState,
            stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit],
            onChange: js.Function1[String, Unit],
            onEnter: js.UndefOr[js.Function0[Unit]],
            onKeypress: js.UndefOr[js.Function1[String, Boolean]] = js.undefined): TextInputProps = {

    js.Dynamic.literal(
      inputRef = inputRef,
      left = left,
      top = top,
      width = width,
      value = value,
      state = state,
      stateUpdater = stateUpdater,
      onChange = onChange,
      onEnter = onEnter,
      onKeypress = onKeypress
    ).asInstanceOf[TextInputProps]
  }

  def unapply(arg: TextInputProps): Option[(NativeRef, Int, Int, Int, String, TextInputState, js.Function1[js.Function1[TextInputState, TextInputState], Unit], js.Function1[String, Unit], js.UndefOr[js.Function0[Unit]], js.UndefOr[js.Function1[String, Boolean]])] = {
    Some((
      arg.inputRef,
      arg.left,
      arg.top,
      arg.width,
      arg.value,
      arg.state,
      arg.stateUpdater,
      arg.onChange,
      arg.onEnter,
      arg.onKeypress
    ))
  }

  def copy(p: TextInputProps)(inputRef: NativeRef = p.inputRef,
                              left: Int = p.left,
                              top: Int = p.top,
                              width: Int = p.width,
                              value: String = p.value,
                              state: TextInputState = p.state,
                              stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit] = p.stateUpdater,
                              onChange: js.Function1[String, Unit] = p.onChange,
                              onEnter: js.UndefOr[js.Function0[Unit]] = p.onEnter,
                              onKeypress: js.UndefOr[js.Function1[String, Boolean]] = p.onKeypress): TextInputProps = {

    TextInputProps(
      inputRef = inputRef,
      left = left,
      top = top,
      width = width,
      value = value,
      state = state,
      stateUpdater = stateUpdater,
      onChange = onChange,
      onEnter = onEnter,
      onKeypress = onKeypress
    )
  }
}
