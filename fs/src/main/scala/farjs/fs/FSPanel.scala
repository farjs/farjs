package farjs.fs

import farjs.filelist._
import farjs.ui.task.{Task, TaskAction}
import scommons.react._
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: ReactClass = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var fsFoldersHistory: ReactClass = FSFoldersHistory
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    val onKeypress: js.Function2[BlessedScreen, String, Boolean] = { (screen, key) =>
      var processed = true
      key match {
        case "M-o" =>
          FileListState.currentItem(props.state).foreach { item =>
            val parent = props.state.currDir.path
            val future = fsService.openItem(parent, item.name).toFuture
            props.dispatch(TaskAction(Task("Opening default app", future)))
          }
        case _ =>
          processed = false
      }

      processed
    }

    <.>()(
      <(fileListPanelComp)(^.plain := FileListPanelProps.copy(props)(onKeypress = onKeypress))(),

      <(fsFreeSpaceComp())(^.plain := FSFreeSpaceProps(
        dispatch = props.dispatch,
        currDir = props.state.currDir
      ))(),

      <(fsFoldersHistory)(^.plain := FSFoldersHistoryProps(
        currDirPath = props.state.currDir.path
      ))()
    )
  }
}
