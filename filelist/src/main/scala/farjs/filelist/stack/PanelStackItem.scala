package farjs.filelist.stack

import farjs.filelist.{FileListActions, FileListData}
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/stack/PanelStackItem.mjs", JSImport.Default)
class PanelStackItem[T](
  val component: ReactClass,
  val dispatch: js.UndefOr[Dispatch],
  val actions: js.UndefOr[FileListActions],
  val state: js.UndefOr[T]
) extends js.Object {

  def withState(s: T): PanelStackItem[T] = js.native
  
  def updateState(f: js.Function1[T, T]): PanelStackItem[T] = js.native
  
  def getData(): js.UndefOr[FileListData] = js.native
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
