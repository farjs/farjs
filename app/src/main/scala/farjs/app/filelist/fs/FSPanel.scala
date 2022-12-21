package farjs.app.filelist.fs

import farjs.filelist.FileListActions._
import farjs.filelist._
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.redux.task.FutureTask

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var fsFoldersHistory: UiComponent[FSFoldersHistoryProps] = FSFoldersHistory
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            val parent = props.state.currDir.path
            val future = fsService.openItem(parent, item.name)
            props.dispatch(FileListTaskAction(FutureTask("Opening default app", future)))
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
      ))(),

      <(fsFoldersHistory())(^.wrapped := FSFoldersHistoryProps(
        currDirPath = props.state.currDir.path
      ))()
    )
  }
}
