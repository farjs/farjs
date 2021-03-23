package farjs.filelist.copy

import farjs.filelist.popups.FileListPopupsActions.FileListPopupCopyItemsAction
import farjs.filelist.popups.FileListPopupsProps
import scommons.react._

object CopyItems extends FunctionComponent[FileListPopupsProps] {

  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val showPopup = props.data.popups.showCopyItemsPopup
    val (fromState, toState) =
      if (props.data.left.isActive) (props.data.left, props.data.right)
      else (props.data.right, props.data.left)

    val items =
      if (fromState.selectedItems.nonEmpty) fromState.selectedItems
      else fromState.currentItem.toList

    if (showPopup) {
      <(copyItemsPopup())(^.wrapped := CopyItemsPopupProps(
        path = toState.currDir.path,
        items = items,
        onCopy = { _ =>
          //TODO: handle onCopy
        },
        onCancel = { () =>
          props.dispatch(FileListPopupCopyItemsAction(show = false))
        }
      ))()
    } else null
  }
}
