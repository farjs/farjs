package farjs.app.filelist.zip

import farjs.filelist.FileListActions._
import farjs.filelist.FileListState
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class AddToZipControllerProps(dispatch: Dispatch,
                                   state: FileListState,
                                   zipName: String,
                                   items: Set[String],
                                   onComplete: String => Unit,
                                   onCancel: () => Unit)

object AddToZipController extends FunctionComponent[AddToZipControllerProps] {

  private[zip] var addToZipPopup: UiComponent[AddToZipPopupProps] = AddToZipPopup
  private[zip] var addToZipApi: (String, String, Set[String]) => Future[Unit] = ZipApi.addToZip

  protected def render(compProps: Props): ReactElement = {
    val (showPopup, setShowPopup) = useState(true)
    val props = compProps.wrapped
    
    if (showPopup) {
      <(addToZipPopup())(^.wrapped := AddToZipPopupProps(
        zipName = props.zipName,
        onAdd = { zipFile =>
          setShowPopup(false)

          val action = addToArchive(zipFile, props.state.currDir.path, props.items)
          props.dispatch(action)
          action.task.future.foreach { _ =>
            if (props.state.selectedNames.nonEmpty) {
              props.dispatch(FileListParamsChangedAction(
                offset = props.state.offset,
                index = props.state.index,
                selectedNames = Set.empty
              ))
            }

            props.onComplete(zipFile)
          }
        },
        onCancel = props.onCancel
      ))()
    }
    else null
  }

  private def addToArchive(zipFile: String, parent: String, items: Set[String]): FileListTaskAction = {
    val future = addToZipApi(zipFile, parent, items)

    FileListTaskAction(FutureTask("Adding files to zip archive", future))
  }
}
