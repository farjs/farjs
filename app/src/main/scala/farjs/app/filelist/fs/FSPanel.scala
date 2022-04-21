package farjs.app.filelist.fs

import farjs.app.filelist.zip.{ZipCreatePopup, ZipCreatePopupProps}
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.FileListItem
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var zipCreatePopup: UiComponent[ZipCreatePopupProps] = ZipCreatePopup
  
  protected def render(compProps: Props): ReactElement = {
    val (showZipPopup, setShowZipPopup) = useState(Option.empty[String])
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(openInDefaultApp(props.state.currDir.path, item.name))
          }
        case "S-f7" =>
          val currItem = props.state.currentItem.filter(_ != FileListItem.up)
          if (props.state.selectedNames.nonEmpty || currItem.nonEmpty) {
            val items = props.state.selectedNames ++ currItem.map(_.name)
            setShowZipPopup(Some(s"${items.head}.zip"))
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

      showZipPopup.map { zipName =>
        <(zipCreatePopup())(^.wrapped := ZipCreatePopupProps(
          zipName = zipName,
          onAdd = { _ =>
            setShowZipPopup(None)
          },
          onCancel = { () =>
            setShowZipPopup(None)
          }
        ))()
      }
    )
  }

  private def openInDefaultApp(parent: String, item: String): FileListTaskAction = {
    val future = fsService.openItem(parent, item)

    FileListTaskAction(FutureTask("Opening default app", future))
  }
}
