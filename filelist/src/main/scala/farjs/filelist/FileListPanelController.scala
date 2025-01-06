package farjs.filelist

import farjs.filelist.stack.WithStack
import scommons.react._

class FileListPanelController(
  fileListPanelComp: UiComponent[FileListPanelProps]
) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val stackProps = WithStack.useStack()
    val stack = stackProps.stack
    val maybeCurrData = {
      val stackItem = stack.peek[FileListState]()
      stackItem.getData()
    }

    maybeCurrData.map { case FileListData(dispatch, actions, state) =>
      <(fileListPanelComp())(^.wrapped := FileListPanelProps(dispatch, actions, state))()
    }.orNull
  }
}
