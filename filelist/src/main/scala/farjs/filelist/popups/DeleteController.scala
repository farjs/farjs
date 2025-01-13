package farjs.filelist.popups

import farjs.filelist.{FileListState, FileListUiData}
import farjs.ui.popup.{MessageBox, MessageBoxAction, MessageBoxProps}
import farjs.ui.theme.Theme
import scommons.react._

import scala.scalajs.js

object DeleteController extends FunctionComponent[FileListUiData] {

  private[popups] var messageBoxComp: ReactClass = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val theme = Theme.useTheme().popup

    props.data.toOption match {
      case Some(data) if props.showDeletePopup.getOrElse(false) =>
        <(messageBoxComp)(^.plain := MessageBoxProps(
          title = "Delete",
          message = "Do you really want to delete selected item(s)?",
          actions = js.Array(
            MessageBoxAction.YES { () =>
              val items =
                if (FileListState.selectedItems(data.state).nonEmpty) FileListState.selectedItems(data.state)
                else js.Array(FileListState.currentItem(data.state).toList: _*)

              props.onClose()
              data.dispatch(data.actions.deleteItems(
                dispatch = data.dispatch,
                parent = data.state.currDir.path,
                items = items
              ))
            },
            MessageBoxAction.NO { () =>
              props.onClose()
            }
          ),
          style = theme.error
        ))()
      case _ => null
    }
  }
}
