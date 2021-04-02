package farjs.filelist.copy

import farjs.filelist.popups.FileListPopupsActions.FileListPopupCopyItemsAction
import farjs.filelist.popups.FileListPopupsProps
import scommons.react._
import scommons.react.hooks._

object CopyItems extends FunctionComponent[FileListPopupsProps] {

  private[copy] var copyItemsStats: UiComponent[CopyItemsStatsProps] = CopyItemsStats
  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup
  private[copy] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup

  protected def render(compProps: Props): ReactElement = {
    val (maybeTotal, setTotal) = useState[Option[Double]](None)
    val (toPath, setToPath) = useState[Option[String]](None)
    val props = compProps.wrapped
    
    val showPopup = props.data.popups.showCopyItemsPopup
    val (fromState, toState) =
      if (props.data.left.isActive) (props.data.left, props.data.right)
      else (props.data.right, props.data.left)

    val items =
      if (fromState.selectedItems.nonEmpty) fromState.selectedItems
      else fromState.currentItem.toList

    def onCancel(dispatchAction: Boolean): () => Unit = { () =>
      if (dispatchAction) {
        props.dispatch(FileListPopupCopyItemsAction(show = false))
      }
      setTotal(None)
      setToPath(None)
    }
    
    <.>()(
      if (showPopup) Some {
        if (maybeTotal.isEmpty) {
          <(copyItemsStats())(^.wrapped := CopyItemsStatsProps(
            dispatch = props.dispatch,
            actions = props.actions,
            state = fromState,
            onDone = { total =>
              setTotal(Some(total))
            },
            onCancel = onCancel(dispatchAction = true)
          ))()
        }
        else {
          <(copyItemsPopup())(^.wrapped := CopyItemsPopupProps(
            path = toState.currDir.path,
            items = items,
            onCopy = { path =>
              props.dispatch(FileListPopupCopyItemsAction(show = false))
              setToPath(Some(path))
            },
            onCancel = onCancel(dispatchAction = true)
          ))()
        }
      }
      else None,

      for {
        to <- toPath
        total <- maybeTotal
      } yield {
        <(copyProgressPopup())(^.wrapped := CopyProgressPopupProps(
          item = "test.file",
          to = to,
          itemPercent = 25,
          total = total,
          totalPercent = 50,
          timeSeconds = 5,
          leftSeconds = 7,
          bytesPerSecond = 345123,
          onCancel = onCancel(dispatchAction = false)
        ))()
      }
    )
  }
}
