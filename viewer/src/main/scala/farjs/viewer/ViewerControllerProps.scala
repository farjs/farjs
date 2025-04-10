package farjs.viewer

import farjs.ui.Dispatch
import scommons.react.raw.NativeRef

import scala.scalajs.js

sealed trait ViewerControllerProps extends js.Object {
  val inputRef: NativeRef
  val dispatch: Dispatch
  val filePath: String
  val size: Double
  val viewport: js.UndefOr[ViewerFileViewport]
  val setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit]
  val onKeypress: js.Function1[String, Boolean]
}

object ViewerControllerProps {

  def apply(inputRef: NativeRef,
            dispatch: Dispatch,
            filePath: String,
            size: Double,
            viewport: js.UndefOr[ViewerFileViewport],
            setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit] = _ => (),
            onKeypress: js.Function1[String, Boolean] = _ => false): ViewerControllerProps = {

    js.Dynamic.literal(
      inputRef = inputRef,
      dispatch = dispatch,
      filePath = filePath,
      size = size,
      viewport = viewport.asInstanceOf[js.Any],
      setViewport = setViewport,
      onKeypress = onKeypress
    ).asInstanceOf[ViewerControllerProps]
  }

  def unapply(arg: ViewerControllerProps): Option[
    (NativeRef, Dispatch, String, Double, js.UndefOr[ViewerFileViewport], js.Function1[js.UndefOr[ViewerFileViewport], Unit], js.Function1[String, Boolean])
  ] = {
    Some((
      arg.inputRef,
      arg.dispatch,
      arg.filePath,
      arg.size,
      arg.viewport,
      arg.setViewport,
      arg.onKeypress
    ))
  }

  def copy(p: ViewerControllerProps)(inputRef: NativeRef = p.inputRef,
                                     dispatch: Dispatch = p.dispatch,
                                     filePath: String = p.filePath,
                                     size: Double = p.size,
                                     viewport: js.UndefOr[ViewerFileViewport] = p.viewport,
                                     setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit] = p.setViewport,
                                     onKeypress: js.Function1[String, Boolean] = p.onKeypress): ViewerControllerProps = {

    ViewerControllerProps(
      inputRef = inputRef,
      dispatch = dispatch,
      filePath = filePath,
      size = size,
      viewport = viewport,
      setViewport = setViewport,
      onKeypress = onKeypress
    )
  }
}
