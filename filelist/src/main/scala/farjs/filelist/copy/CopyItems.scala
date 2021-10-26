package farjs.filelist.copy

import farjs.filelist.FileListActions.{FileListParamsChangedAction, FileListTaskAction}
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.FileListPopupsProps
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs.path
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CopyItems extends FunctionComponent[FileListPopupsProps] {

  private[copy] var copyItemsStats: UiComponent[CopyItemsStatsProps] = CopyItemsStats
  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup
  private[copy] var copyProcessComp: UiComponent[CopyProcessProps] = CopyProcess
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val (maybeTotal, setTotal) = useState[Option[Double]](None)
    val (maybeToPath, setToPath) = useState[Option[String]](None)
    val (move, setMove) = useState(false)
    val (needTotal, setNeedTotal) = useState(false)
    val copied = useRef(Set.empty[String])

    val props = compProps.wrapped
    val showPopup = props.data.popups.showCopyItemsPopup || props.data.popups.showMoveItemsPopup
    
    val (fromState, toState) =
      if (props.data.left.isActive) (props.data.left, props.data.right)
      else (props.data.right, props.data.left)

    val items =
      if (fromState.selectedItems.nonEmpty) fromState.selectedItems
      else fromState.currentItem.toList

    def onCancel(dispatchAction: Boolean): () => Unit = { () =>
      if (dispatchAction) {
        if (props.data.popups.showMoveItemsPopup) {
          props.dispatch(FileListPopupMoveItemsAction(show = false))
        }
        else props.dispatch(FileListPopupCopyItemsAction(show = false))
      }
      setTotal(None)
      setToPath(None)
      setMove(false)
      setNeedTotal(false)
      copied.current = Set.empty[String]
    }
    
    val onDone: () => Unit = { () =>
      val updatedSelection = fromState.selectedNames -- copied.current
      if (updatedSelection != fromState.selectedNames) {
        props.dispatch(FileListParamsChangedAction(
          isRight = fromState.isRight,
          offset = fromState.offset,
          index = fromState.index,
          selectedNames = updatedSelection
        ))
      }
      
      onCancel(dispatchAction = false)()

      val leftAction = props.actions.updateDir(props.dispatch, isRight = false, props.data.left.currDir.path)
      props.dispatch(leftAction)

      leftAction.task.future.andThen {
        case Success(_) =>
          val rightAction = props.actions.updateDir(props.dispatch, isRight = true, props.data.right.currDir.path)
          props.dispatch(rightAction)
      }
    }

    val fromPath = fromState.currDir.path
    val maybeError = maybeToPath.flatMap { toPath =>
      val op = if (move) "move" else "copy"
      if (fromPath == toPath) Some {
        s"Cannot $op the item\n${items.head.name}\nonto itself"
      }
      else if (toPath.startsWith(fromPath + path.sep)) {
        val toSuffix = toPath.stripPrefix(fromPath + path.sep)
        val maybeSelf = items.find(i => toSuffix == i.name || toSuffix.startsWith(i.name + path.sep))
        maybeSelf.map { self =>
          s"Cannot $op the item\n${self.name}\ninto itself"
        }
      }
      else None
    }
    
    <.>()(
      if (showPopup) Some {
        <(copyItemsPopup())(^.wrapped := CopyItemsPopupProps(
          move = props.data.popups.showMoveItemsPopup,
          path = toState.currDir.path,
          items = items,
          onAction = { path =>
            val dirF = props.actions.readDir(Some(fromPath), path)
            dirF.onComplete {
              case Success(dir) =>
                setMove(props.data.popups.showMoveItemsPopup)
                if (props.data.popups.showMoveItemsPopup) {
                  props.dispatch(FileListPopupMoveItemsAction(show = false))
                }
                else props.dispatch(FileListPopupCopyItemsAction(show = false))
                setToPath(Some(dir.path))
                setNeedTotal(true)
              case Failure(_) =>
                props.dispatch(FileListTaskAction(FutureTask("Resolving target dir", dirF)))
            }
          },
          onCancel = onCancel(dispatchAction = true)
        ))()
      }
      else None,

      if (needTotal && maybeError.isEmpty) Some {
        <(copyItemsStats())(^.wrapped := CopyItemsStatsProps(
          dispatch = props.dispatch,
          actions = props.actions,
          state = fromState,
          title = if (move) "Move" else "Copy",
          onDone = { total =>
            setNeedTotal(false)
            setTotal(Some(total))
          },
          onCancel = onCancel(dispatchAction = false)
        ))()
      }
      else maybeError.map { error =>
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Error",
          message = error,
          actions = List(MessageBoxAction.OK(onCancel(dispatchAction = false))),
          style = Theme.current.popup.error
        ))()
      },

      for {
        toPath <- maybeToPath
        total <- maybeTotal if maybeError.isEmpty
      } yield {
        <(copyProcessComp())(^.wrapped := CopyProcessProps(
          dispatch = props.dispatch,
          actions = props.actions,
          move = move,
          fromPath = fromPath,
          items = items,
          toPath = toPath,
          total = total,
          onTopItem = { item =>
            copied.current += item.name
          },
          onDone = onDone
        ))()
      }
    )
  }
}
