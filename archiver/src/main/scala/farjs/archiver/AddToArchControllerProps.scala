package farjs.archiver

import farjs.filelist.{FileListActions, FileListState}
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait AddToArchControllerProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val archName: String
  val archType: String
  val archAction: AddToArchAction
  val addToArchApi: js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]]
  val items: js.Array[FileListItem]
  val onComplete: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object AddToArchControllerProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            archName: String,
            archType: String,
            archAction: AddToArchAction,
            addToArchApi: js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]],
            items: js.Array[FileListItem],
            onComplete: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): AddToArchControllerProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      archName = archName,
      archType = archType,
      archAction = archAction,
      addToArchApi = addToArchApi,
      items = items,
      onComplete = onComplete,
      onCancel = onCancel
    ).asInstanceOf[AddToArchControllerProps]
  }

  def unapply(arg: AddToArchControllerProps): Option[
    (Dispatch, FileListActions, FileListState, String, String, AddToArchAction, js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]], js.Array[FileListItem], js.Function1[String, Unit], js.Function0[Unit])
  ] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.archName,
      arg.archType,
      arg.archAction,
      arg.addToArchApi,
      arg.items,
      arg.onComplete,
      arg.onCancel
    ))
  }
}
