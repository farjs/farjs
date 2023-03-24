package farjs.filelist.popups

import farjs.filelist.{FileListServices, FileListUiData}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

object MakeFolderController extends FunctionComponent[FileListUiData] {

  private[popups] var makeFolderPopup: UiComponent[MakeFolderPopupProps] = MakeFolderPopup

  private var initialMultiple = false

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (multiple, setMultiple) = useState(initialMultiple)
    val props = compProps.wrapped

    props.data match {
      case Some(data) if props.showMkFolderPopup =>
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
              initialMultiple = multiple

              props.onClose()
            }
            data.dispatch(action)
          },
          onCancel = props.onClose
        ))()
      case _ => null
    }
  }
}
