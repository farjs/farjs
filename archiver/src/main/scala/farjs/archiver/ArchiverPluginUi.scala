package farjs.archiver

import farjs.archiver.ArchiverPluginUi._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

class ArchiverPluginUi(data: FileListData, archName: String, archType: String, addToArchApi: js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]], items: js.Array[FileListItem])
    extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <(addToArchController)(^.plain := AddToArchControllerProps(
      dispatch = data.dispatch,
      actions = data.actions,
      state = data.state,
      archName = archName,
      archType = archType,
      archAction = AddToArchAction.Add,
      addToArchApi = addToArchApi,
      items = items,
      onComplete = { archFile =>
        props.onClose()

        val action = data.actions.updateDir(data.dispatch, data.state.currDir.path)
        data.dispatch(action)
        action.task.result.toFuture.foreach { updatedDir =>
          data.dispatch(FileListItemCreatedAction(archFile, updatedDir.asInstanceOf[FileListDir]))
        }
      },
      onCancel = props.onClose
    ))()
  }
}

object ArchiverPluginUi {

  private[archiver] var addToArchController: ReactClass = AddToArchController
}
