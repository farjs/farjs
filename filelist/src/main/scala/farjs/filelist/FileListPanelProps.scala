package farjs.filelist

import farjs.ui.Dispatch
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js

sealed trait FileListPanelProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val onKeypress: js.UndefOr[js.Function2[BlessedScreen, String, Boolean]]
}

object FileListPanelProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            onKeypress: js.UndefOr[js.Function2[BlessedScreen, String, Boolean]] = js.undefined): FileListPanelProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      onKeypress = onKeypress
    ).asInstanceOf[FileListPanelProps]
  }

  def unapply(arg: FileListPanelProps): Option[
    (Dispatch, FileListActions, FileListState, js.UndefOr[js.Function2[BlessedScreen, String, Boolean]])
  ] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.onKeypress
    ))
  }

  def copy(p: FileListPanelProps)(dispatch: Dispatch = p.dispatch,
                                  actions: FileListActions = p.actions,
                                  state: FileListState = p.state,
                                  onKeypress: js.UndefOr[js.Function2[BlessedScreen, String, Boolean]] = p.onKeypress): FileListPanelProps = {

    FileListPanelProps(
      dispatch = dispatch,
      actions = actions,
      state = state,
      onKeypress = onKeypress
    )
  }
}
