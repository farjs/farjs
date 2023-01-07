package farjs.archiver

import farjs.archiver.ArchiverPluginUi._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.FileListItem
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global

class ArchiverPluginUi(data: FileListData, zipName: String, items: Seq[FileListItem])
    extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <(addToArchController())(^.wrapped := AddToArchControllerProps(
      dispatch = data.dispatch,
      actions = data.actions,
      state = data.state,
      zipName = zipName,
      items = items,
      action = AddToArchAction.Add,
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

object ArchiverPluginUi {

  private[archiver] var addToArchController: UiComponent[AddToArchControllerProps] = AddToArchController
}
