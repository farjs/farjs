package farjs.filelist

import farjs.ui.Dispatch
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js

sealed trait FileListProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val width: Int
  val height: Int
  val columns: Int
  val onKeypress: js.Function2[BlessedScreen, String, Unit]
}

object FileListProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            width: Int,
            height: Int,
            columns: Int,
            onKeypress: js.Function2[BlessedScreen, String, Unit] = (_, _) => ()): FileListProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      width = width,
      height = height,
      columns = columns,
      onKeypress = onKeypress
    ).asInstanceOf[FileListProps]
  }

  def unapply(arg: FileListProps): Option[
    (Dispatch, FileListActions, FileListState, Int, Int, Int, js.Function2[BlessedScreen, String, Unit])
  ] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.width,
      arg.height,
      arg.columns,
      arg.onKeypress
    ))
  }

  def copy(p: FileListProps)(dispatch: Dispatch = p.dispatch,
                             actions: FileListActions = p.actions,
                             state: FileListState = p.state,
                             width: Int = p.width,
                             height: Int = p.height,
                             columns: Int = p.columns,
                             onKeypress: js.Function2[BlessedScreen, String, Unit] = p.onKeypress): FileListProps = {

    FileListProps(
      dispatch = dispatch,
      actions = actions,
      state = state,
      width = width,
      height = height,
      columns = columns,
      onKeypress = onKeypress
    )
  }
}
