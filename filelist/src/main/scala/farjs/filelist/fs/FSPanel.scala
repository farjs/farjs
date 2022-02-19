package farjs.filelist.fs

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.redux.task.FutureTask

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(openInDefaultApp(props.state.currDir.path, item.name))
          }
        case _ =>
          processed = false
      }

      processed
    }

    <.>()(
      <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))(),

      <(fsFreeSpaceComp())(^.wrapped := FSFreeSpaceProps(
        dispatch = props.dispatch,
        currDir = props.state.currDir
      ))()
    )
  }

  private def openInDefaultApp(parent: String, item: String): FileListTaskAction = {
    val future = fsService.openItem(parent, item)

    FileListTaskAction(FutureTask("Opening default app", future))
  }
}
