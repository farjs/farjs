package farjs.filelist

import farjs.ui.Dispatch
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js

sealed trait FileListPanelViewProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val onKeypress: js.Function2[BlessedScreen, String, Unit]
}

object FileListPanelViewProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            onKeypress: js.Function2[BlessedScreen, String, Unit] = (_, _) => ()): FileListPanelViewProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      onKeypress = onKeypress
    ).asInstanceOf[FileListPanelViewProps]
  }

  def unapply(arg: FileListPanelViewProps): Option[(Dispatch, FileListActions, FileListState, js.Function2[BlessedScreen, String, Unit])] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.onKeypress
    ))
  }
}
