package farjs.filelist

import farjs.filelist.stack.PanelStackComp
import scommons.react._
import scommons.react.hooks._

class FileListPanelController(
  fileListPanelComp: UiComponent[FileListPanelProps]
) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val stackProps = PanelStackComp.usePanelStack
    val stack = stackProps.stack
    val maybeCurrData = {
      val stackItem = stack.peek[FileListState]
      stackItem.getData
    }

    useLayoutEffect({ () =>
      stack.update[FileListState](
        _.updateState(FileListState.copy(_)(isActive = stack.isActive))
      )
      ()
    }, List(stack.isActive))
    
    maybeCurrData.map { case FileListData(dispatch, actions, state) =>
      <(fileListPanelComp())(^.wrapped := FileListPanelProps(dispatch, actions, state))()
    }.orNull
  }
}
