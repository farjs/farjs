package farjs.app.filelist

import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait FileListBrowserProps extends js.Object {
  val dispatch: Dispatch
  val isRightInitiallyActive: Boolean
}

object FileListBrowserProps {

  def apply(dispatch: Dispatch, isRightInitiallyActive: Boolean = false): FileListBrowserProps = {
    js.Dynamic.literal(
      dispatch = dispatch,
      isRightInitiallyActive = isRightInitiallyActive
    ).asInstanceOf[FileListBrowserProps]
  }

  def unapply(arg: FileListBrowserProps): Option[(Dispatch, Boolean)] = {
    Some((
      arg.dispatch,
      arg.isRightInitiallyActive
    ))
  }
}
