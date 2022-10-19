package farjs.filelist.popups

import farjs.filelist.FileListServices
import farjs.filelist.popups.FileListPopupsActions._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

object MakeFolderController extends FunctionComponent[PopupControllerProps] {

  private[popups] var makeFolderPopup: UiComponent[MakeFolderPopupProps] = MakeFolderPopup

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (multiple, setMultiple) = useState(false)
    val props = compProps.wrapped
    val popups = props.popups

    props.data match {
      case Some(data) if popups.showMkFolderPopup =>
        <(makeFolderPopup())(^.wrapped := MakeFolderPopupProps(
          multiple = multiple,
          onOk = { (dir, multiple) =>
            val action = data.actions.createDir(
              dispatch = data.dispatch,
              parent = data.state.currDir.path,
              dir = dir,
              multiple = multiple
            )
            for {
              _ <- action.task.future
              _ <- services.mkDirsHistory.save(dir)
            } yield {
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
