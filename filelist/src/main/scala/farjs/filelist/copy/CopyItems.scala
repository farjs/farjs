package farjs.filelist.copy

import farjs.filelist.popups.FileListPopupsActions.FileListPopupCopyItemsAction
import farjs.filelist.popups.FileListPopupsProps
import scommons.react._
import scommons.react.hooks._

object CopyItems extends FunctionComponent[FileListPopupsProps] {

  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup
  private[copy] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup

  protected def render(compProps: Props): ReactElement = {
    val (toPath, setToPath) = useState[Option[String]](None)
    val props = compProps.wrapped
    
    val showPopup = props.data.popups.showCopyItemsPopup
    val (fromState, toState) =
      if (props.data.left.isActive) (props.data.left, props.data.right)
      else (props.data.right, props.data.left)

    val items =
      if (fromState.selectedItems.nonEmpty) fromState.selectedItems
      else fromState.currentItem.toList

    <.>()(
      if (showPopup) Some(
        <(copyItemsPopup())(^.wrapped := CopyItemsPopupProps(
          path = toState.currDir.path,
          items = items,
          onCopy = { path =>
            setToPath(Some(path))
          },
          onCancel = { () =>
            props.dispatch(FileListPopupCopyItemsAction(show = false))
          }
        ))()
      )
      else None,

      toPath.map { to =>
        <(copyProgressPopup())(^.wrapped := CopyProgressPopupProps(
          item = "test.file",
          to = to,
          itemPercent = 25,
          total = 1234567890,
          totalPercent = 50,
          timeSeconds = 5,
          leftSeconds = 7,
          bytesPerSecond = 345123,
          onCancel = { () =>
            setToPath(None)
          }
        ))()
      }
    )
  }
}
