package farjs.archiver

import farjs.archiver.ArchiverPluginUi._
import farjs.archiver.zip.ZipApi
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

class ArchiverPluginUi(data: FileListData, zipName: String, items: Seq[FileListItem])
    extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <(addToArchController)(^.plain := AddToArchControllerProps(
      dispatch = data.dispatch,
      actions = data.actions,
      state = data.state,
      archName = zipName,
      archType = "zip",
      archAction = AddToArchAction.Add,
      addToArchApi = ZipApi.addToZip _,
      items = js.Array(items: _*),
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
