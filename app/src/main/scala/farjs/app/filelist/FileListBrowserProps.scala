package farjs.app.filelist

import farjs.filelist.FileListPlugin
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait FileListBrowserProps extends js.Object {
  val dispatch: Dispatch
  val isRightInitiallyActive: Boolean
  val plugins: js.Array[FileListPlugin]
}

object FileListBrowserProps {

  def apply(dispatch: Dispatch,
            isRightInitiallyActive: Boolean = false,
            plugins: js.Array[FileListPlugin] = js.Array()): FileListBrowserProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      isRightInitiallyActive = isRightInitiallyActive,
      plugins = plugins
    ).asInstanceOf[FileListBrowserProps]
  }

  def unapply(arg: FileListBrowserProps): Option[(Dispatch, Boolean, js.Array[FileListPlugin])] = {
    Some((
      arg.dispatch,
      arg.isRightInitiallyActive,
      arg.plugins
    ))
  }
}
