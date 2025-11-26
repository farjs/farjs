package farjs.archiver

import farjs.filelist.{FileListActions, FileListState}
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait AddToArchControllerProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val zipName: String
  val items: js.Array[FileListItem]
  val action: AddToArchAction
  val onComplete: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object AddToArchControllerProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            zipName: String,
            items: js.Array[FileListItem],
            action: AddToArchAction,
            onComplete: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): AddToArchControllerProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      zipName = zipName,
      items = items,
      action = action,
      onComplete = onComplete,
      onCancel = onCancel
    ).asInstanceOf[AddToArchControllerProps]
  }

  def unapply(arg: AddToArchControllerProps): Option[(Dispatch, FileListActions, FileListState, String, js.Array[FileListItem], AddToArchAction, js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.zipName,
      arg.items,
      arg.action,
      arg.onComplete,
      arg.onCancel
    ))
  }
}
