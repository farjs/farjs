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
  private[fs] var fsFoldersHistory: UiComponent[FSFoldersHistoryProps] = FSFoldersHistory
  private[fs] var addToZipController: UiComponent[AddToZipControllerProps] = AddToZipController
  
  protected def render(compProps: Props): ReactElement = {
    val (showFoldersHistory, setShowFoldersHistory) = useState(false)
    val (zipData, setZipData) = useState(Option.empty[(String, Seq[FileListItem])])
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-h" => setShowFoldersHistory(true)
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(openInDefaultApp(props.state.currDir.path, item.name))
          }
        case "S-f7" =>
          val items =
            if (props.state.selectedNames.nonEmpty) props.state.selectedItems
            else {
              val currItem = props.state.currentItem.filter(_ != FileListItem.up)
              currItem.toList
            }
          if (items.nonEmpty) {
            setZipData(Some((s"${items.head.name}.zip", items)))
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
        showPopup = showFoldersHistory,
        currDirPath = props.state.currDir.path,
        onChangeDir = { dir =>
          setShowFoldersHistory(false)

          if (dir != props.state.currDir.path) {
            props.dispatch(props.actions.changeDir(
              dispatch = props.dispatch,
              parent = None,
              dir = dir
            ))
          }
        },
        onHidePopup = { () =>
          setShowFoldersHistory(false)
        }
      ))(),

      zipData.map { case (zipName, items) =>
        <(addToZipController())(^.wrapped := AddToZipControllerProps(
          dispatch = props.dispatch,
          actions = props.actions,
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
