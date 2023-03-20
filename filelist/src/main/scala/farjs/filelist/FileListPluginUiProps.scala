package farjs.filelist

import scommons.react.redux.Dispatch

import scala.scalajs.js

sealed trait FileListPluginUiProps extends js.Object {
  
  def dispatch: Dispatch
  def onClose: js.Function0[Unit]
}

object FileListPluginUiProps {

  def apply(dispatch: Dispatch, onClose: js.Function0[Unit]): FileListPluginUiProps = {
    js.Dynamic.literal(
      dispatch = dispatch.asInstanceOf[js.Any],
      onClose = onClose
    ).asInstanceOf[FileListPluginUiProps]
  }

  def unapply(arg: FileListPluginUiProps): Option[(Dispatch, js.Function0[Unit])] = {
    Some((
      arg.dispatch,
      arg.onClose
    ))
  }
}
