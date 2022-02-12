package farjs.filelist.fs

import farjs.filelist._
import farjs.filelist.stack.PanelStack
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch

import scala.scalajs.js

case class FSPanelProps(dispatch: Dispatch,
                        actions: FileListActions,
                        data: FileListsStateDef)

object FSPanel extends FunctionComponent[FSPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  
  protected def render(compProps: Props): ReactElement = {
    val stackProps = PanelStack.usePanelStack
    val props = compProps.wrapped
    val state =
      if (stackProps.isRight) props.data.right
      else props.data.left

    useLayoutEffect({ () =>
      stackProps.stack.update[FileListState](
        _.withActions(props.dispatch, props.actions)
          .withState(state)
      )
      ()
    }, List(state.asInstanceOf[js.Any]))
    
    <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions, state))()
  }
}
