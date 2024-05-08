package farjs.filelist.stack

import farjs.filelist.{FileListActions, FileListData, FileListState}
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.UndefOr

class PanelStackItem[T](
  val component: ReactClass,
  val dispatch: js.UndefOr[Dispatch],
  val actions: js.UndefOr[FileListActions],
  val state: js.UndefOr[T]
) extends js.Object {

  def withState(s: T): PanelStackItem[T] = {
    new PanelStackItem[T](component, dispatch, actions, s)
  }
  
  def updateState(f: T => T): PanelStackItem[T] = {
    new PanelStackItem[T](component, dispatch, actions, state.map(f))
  }
  
  def getData: js.UndefOr[FileListData] = {
    (dispatch.toOption, actions.toOption, state.toOption) match {
      case (Some(dispatch), Some(actions), Some(state)) if FileListState.isFileListState(state.asInstanceOf[js.Any]) =>
        FileListData(dispatch, actions, state.asInstanceOf[FileListState])
      case _ => js.undefined
    }
  }
}

object PanelStackItem {

  def apply[T](component: ReactClass,
               dispatch: UndefOr[Dispatch] = js.undefined,
               actions: UndefOr[FileListActions] = js.undefined,
               state: UndefOr[T] = js.undefined
              ): PanelStackItem[T] = {

    new PanelStackItem[T](component, dispatch, actions, state)
  }

  def unapply(arg: PanelStackItem[_]): Option[(ReactClass, js.UndefOr[Dispatch], js.UndefOr[FileListActions], js.UndefOr[_])] = {
    Some((
      arg.component,
      arg.dispatch,
      arg.actions,
      arg.state
    ))
  }

  def copy[T](p: PanelStackItem[T])(component: ReactClass = p.component,
                                    dispatch: js.UndefOr[Dispatch] = p.dispatch,
                                    actions: js.UndefOr[FileListActions] = p.actions,
                                    state: js.UndefOr[T] = p.state): PanelStackItem[T] = {
    new PanelStackItem[T](
      component = component,
      dispatch = dispatch,
      actions = actions,
      state = state
    )
  }
}
