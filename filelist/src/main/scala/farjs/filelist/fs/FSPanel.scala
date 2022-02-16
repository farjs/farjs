package farjs.filelist.fs

import farjs.filelist._
import farjs.filelist.stack.PanelStack
import scommons.react._
import scommons.react.hooks._

object FSPanel extends FunctionComponent[Unit] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  
  protected def render(compProps: Props): ReactElement = {
    val stackProps = PanelStack.usePanelStack
    val stack = stackProps.stack
    val maybeCurrData = {
      val stackItem = stack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state)
    }

    useLayoutEffect({ () =>
      stack.update[FileListState](
        _.updateState(_.copy(isActive = stack.isActive))
      )
      ()
    }, List(stack.isActive))
    
    maybeCurrData.map { case ((dispatch, actions), state) =>
      <(fileListPanelComp())(^.wrapped := FileListPanelProps(dispatch, actions, state))()
    }.orNull
  }
}
