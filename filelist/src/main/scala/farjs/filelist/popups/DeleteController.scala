package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui.popup.{MessageBox, MessageBoxAction, MessageBoxProps}
import farjs.ui.theme.Theme
import scommons.react._

import scala.scalajs.js

object DeleteController extends FunctionComponent[PopupControllerProps] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val popups = props.popups
    val theme = Theme.current.popup

    props.data match {
      case Some(data) if popups.showDeletePopup =>
        <(messageBoxComp())(^.plain := MessageBoxProps(
          title = "Delete",
          message = "Do you really want to delete selected item(s)?",
          actions = js.Array(
            MessageBoxAction.YES { () =>
              val items =
                if (data.state.selectedItems.nonEmpty) data.state.selectedItems
                else data.state.currentItem.toList

              data.dispatch(FileListPopupDeleteAction(show = false))
              data.dispatch(data.actions.deleteAction(
                dispatch = data.dispatch,
                dir = data.state.currDir.path,
                items = items
              ))
            },
            MessageBoxAction.NO { () =>
              data.dispatch(FileListPopupDeleteAction(show = false))
            }
          ),
          style = theme.error
        ))()
      case _ => null
    }
  }
}
