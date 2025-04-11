package farjs.viewer.quickview

import farjs.ui.Dispatch
import scommons.react.raw.NativeRef

import scala.scalajs.js

sealed trait QuickViewFileProps extends js.Object {
  val dispatch: Dispatch
  val inputRef: NativeRef
  val isRight: Boolean
  val filePath: String
  val size: Double
}

object QuickViewFileProps {

  def apply(dispatch: Dispatch,
            inputRef: NativeRef,
            isRight: Boolean,
            filePath: String,
            size: Double): QuickViewFileProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      inputRef = inputRef,
      isRight = isRight,
      filePath = filePath,
      size = size
    ).asInstanceOf[QuickViewFileProps]
  }

  def unapply(arg: QuickViewFileProps): Option[(Dispatch, NativeRef, Boolean, String, Double)] = {
    Some((
      arg.dispatch,
      arg.inputRef,
      arg.isRight,
      arg.filePath,
      arg.size
    ))
  }
}
