package farjs.app.filelist.fs

import farjs.app.filelist.zip._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.FileListItem
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var zipCreatePopup: UiComponent[ZipCreatePopupProps] = ZipCreatePopup
  private[fs] var addToZip: (String, String, Set[String]) => Future[Unit] = ZipApi.addToZip
  
  protected def render(compProps: Props): ReactElement = {
    val (zipData, setZipData) = useState(Option.empty[(String, Set[String])])
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(openInDefaultApp(props.state.currDir.path, item.name))
          }
        case "S-f7" =>
          val items =
            if (props.state.selectedNames.nonEmpty) props.state.selectedNames
            else {
              val currItem = props.state.currentItem.filter(_ != FileListItem.up)
              currItem.map(_.name).toSet
            }
          if (items.nonEmpty) {
            setZipData(Some((s"${items.head}.zip", items)))
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

      zipData.map { case (zipName, items) =>
        <(zipCreatePopup())(^.wrapped := ZipCreatePopupProps(
          zipName = zipName,
          onAdd = { zipFile =>
            val action = createZipArchive(zipFile, props.state.currDir.path, items)
            props.dispatch(action)
            setZipData(None)

            action.task.future.foreach { _ =>
              if (props.state.selectedNames.nonEmpty) {
                props.dispatch(FileListParamsChangedAction(
                  offset = props.state.offset,
                  index = props.state.index,
                  selectedNames = Set.empty
                ))
              }
              props.actions.readDir(None, props.state.currDir.path).foreach { updatedDir =>
                props.dispatch(FileListItemCreatedAction(zipFile, updatedDir))
              }
            }
          },
          onCancel = { () =>
            setZipData(None)
          }
        ))()
      }
    )
  }

  private def openInDefaultApp(parent: String, item: String): FileListTaskAction = {
    val future = fsService.openItem(parent, item)

    FileListTaskAction(FutureTask("Opening default app", future))
  }

  private def createZipArchive(zipFile: String, parent: String, items: Set[String]): FileListTaskAction = {
    val future = addToZip(zipFile, parent, items)

    FileListTaskAction(FutureTask("Creating zip archive", future))
  }
}
