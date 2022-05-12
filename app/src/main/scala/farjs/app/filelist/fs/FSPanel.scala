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

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[fs] var fsFreeSpaceComp: UiComponent[FSFreeSpaceProps] = FSFreeSpace
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var addToZipController: UiComponent[AddToZipControllerProps] = AddToZipController
  
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
        <(addToZipController())(^.wrapped := AddToZipControllerProps(
          dispatch = props.dispatch,
          state = props.state,
          zipName = zipName,
          items = items,
          action = AddToZipAction.Add,
          onComplete = { zipFile =>
            setZipData(None)

            val action = props.actions.updateDir(props.dispatch, props.state.currDir.path)
            props.dispatch(action)
            action.task.future.foreach { updatedDir =>
              props.dispatch(FileListItemCreatedAction(zipFile, updatedDir))
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
}
