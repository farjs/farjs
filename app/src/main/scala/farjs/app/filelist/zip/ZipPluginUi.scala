package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPluginUi._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.FileListItem
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global

class ZipPluginUi(data: FileListData, zipName: String, items: Seq[FileListItem])
    extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <(addToZipController())(^.wrapped := AddToZipControllerProps(
      dispatch = data.dispatch,
      actions = data.actions,
      state = data.state,
      zipName = zipName,
      items = items,
      action = AddToZipAction.Add,
      onComplete = { zipFile =>
        props.onClose()

        val action = data.actions.updateDir(data.dispatch, data.state.currDir.path)
        data.dispatch(action)
        action.task.future.foreach { updatedDir =>
          data.dispatch(FileListItemCreatedAction(zipFile, updatedDir))
        }
      },
      onCancel = props.onClose
    ))()
  }
}

object ZipPluginUi {

  private[zip] var addToZipController: UiComponent[AddToZipControllerProps] = AddToZipController
}
