package farjs.filelist.fs

import farjs.filelist._
import farjs.filelist.stack.PanelStack
import scommons.react._
import scommons.react.redux.Dispatch

case class FSPanelProps(dispatch: Dispatch,
                        actions: FileListActions,
                        data: FileListsStateDef)

object FSPanel extends FunctionComponent[FSPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  
  protected def render(compProps: Props): ReactElement = {
    val panelStack = PanelStack.usePanelStack
    val props = compProps.wrapped
    val state =
      if (panelStack.isRight) props.data.right
      else props.data.left

    <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions, state))()
  }
}
