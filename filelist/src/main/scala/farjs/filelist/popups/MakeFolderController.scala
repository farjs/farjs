package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

object MakeFolderController extends FunctionComponent[PopupControllerProps] {

  private[popups] var makeFolderPopup: UiComponent[MakeFolderPopupProps] = MakeFolderPopup

  protected def render(compProps: Props): ReactElement = {
    val (folderItems, setFolderItems) = useState[List[String]](Nil)
    val (folderName, setFolderName) = useState("")
    val (multiple, setMultiple) = useState(false)
    val props = compProps.wrapped
    val popups = props.popups

    props.data match {
      case Some(data) if popups.showMkFolderPopup =>
        <(makeFolderPopup())(^.wrapped := MakeFolderPopupProps(
          folderItems = folderItems,
          folderName = folderName,
          multiple = multiple,
          onOk = { (dir, multiple) =>
            val action = data.actions.createDir(
              dispatch = data.dispatch,
              parent = data.state.currDir.path,
              dir = dir,
              multiple = multiple
            )
            action.task.future.foreach { _ =>
              setFolderItems((dir :: folderItems).distinct)
              setFolderName(dir)
              setMultiple(multiple)
              data.dispatch(FileListPopupMkFolderAction(show = false))
            }
            data.dispatch(action)
          },
          onCancel = { () =>
            data.dispatch(FileListPopupMkFolderAction(show = false))
          }
        ))()
      case _ => null
    }
  }
}
