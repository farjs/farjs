package farjs.viewer

import scommons.react.raw.NativeRef

import scala.scalajs.js

sealed trait ViewerContentProps extends js.Object {
  val inputRef: NativeRef
  val viewport: ViewerFileViewport
  val setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit]
  val onKeypress: js.Function1[String, Boolean]
}

object ViewerContentProps {

  def apply(inputRef: NativeRef,
            viewport: ViewerFileViewport,
            setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit],
            onKeypress: js.Function1[String, Boolean]): ViewerContentProps = {

    js.Dynamic.literal(
      inputRef = inputRef,
      viewport = viewport.asInstanceOf[js.Any],
      setViewport = setViewport,
      onKeypress = onKeypress
    ).asInstanceOf[ViewerContentProps]
  }

  def unapply(arg: ViewerContentProps): Option[(NativeRef, ViewerFileViewport, js.Function1[js.UndefOr[ViewerFileViewport], Unit], js.Function1[String, Boolean])] = {
    Some((
      arg.inputRef,
      arg.viewport,
      arg.setViewport,
      arg.onKeypress
    ))
  }

  def copy(p: ViewerContentProps)(inputRef: NativeRef = p.inputRef,
                                  viewport: ViewerFileViewport = p.viewport,
                                  setViewport: js.Function1[js.UndefOr[ViewerFileViewport], Unit] = p.setViewport,
                                  onKeypress: js.Function1[String, Boolean] = p.onKeypress): ViewerContentProps = {

    ViewerContentProps(
      inputRef = inputRef,
      viewport = viewport,
      setViewport = setViewport,
      onKeypress = onKeypress
    )
  }
}
