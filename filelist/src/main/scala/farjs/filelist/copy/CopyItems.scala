package farjs.filelist.copy

import farjs.filelist.FileListActions.{FileListParamsChangedAction, FileListTaskAction}
import farjs.filelist.api.FileListItem
import farjs.filelist.fs.FSService
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.FileListPopupsProps
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs.path
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object CopyItems extends FunctionComponent[FileListPopupsProps] {

  private[copy] var copyItemsStats: UiComponent[CopyItemsStatsProps] = CopyItemsStats
  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup
  private[copy] var copyProcessComp: UiComponent[CopyProcessProps] = CopyProcess
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  private[copy] var moveProcessComp: UiComponent[MoveProcessProps] = MoveProcess
  private[copy] var fsService: FSService = FSService.instance

  protected def render(compProps: Props): ReactElement = {
    val (maybeTotal, setTotal) = useState[Option[Double]](None)
    val (maybeToPath, setToPath) = useState[Option[String]](None)
    val (inplace, setInplace) = useState(false)
    val (move, setMove) = useState(false)
    val (showStats, setShowStats) = useState(false)
    val (showMove, setShowMove) = useState(false)
    val copied = useRef(Set.empty[String])

    val props = compProps.wrapped
    val showPopup = props.data.popups.showCopyMovePopup != CopyMoveHidden
    
    val (fromState, toState) =
      if (props.data.left.isActive) (props.data.left, props.data.right)
      else (props.data.right, props.data.left)

    def onCancel(dispatchAction: Boolean): () => Unit = { () =>
      if (dispatchAction) {
        props.dispatch(FileListPopupCopyMoveAction(CopyMoveHidden))
      }
      setTotal(None)
      setToPath(None)
      setInplace(false)
      setMove(false)
      setShowStats(false)
      setShowMove(false)
      copied.current = Set.empty[String]
    }

    def onTopItem(item: FileListItem): Unit = copied.current += item.name
    
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

    def isMove: Boolean =
      move ||
      props.data.popups.showCopyMovePopup == ShowMoveToTarget ||
        props.data.popups.showCopyMovePopup == ShowMoveInplace

    def isInplace: Boolean = {
      inplace ||
      props.data.popups.showCopyMovePopup == ShowCopyInplace ||
        props.data.popups.showCopyMovePopup == ShowMoveInplace
    }

    val fromPath = fromState.currDir.path
    val items =
      if (!isInplace && fromState.selectedItems.nonEmpty) fromState.selectedItems
      else fromState.currentItem.toList
    
    def onAction(path: String): Unit = {
      val move = isMove
      val inplace = isInplace
      val resolveF =
        if (!inplace) resolveTargetDir(move, path)
        else Future.successful((path, true))
      
      resolveF.map { case (toPath, sameDrive) =>
        setInplace(inplace)
        setMove(move)
        props.dispatch(FileListPopupCopyMoveAction(CopyMoveHidden))

        setToPath(Some(toPath))
        if (move && sameDrive) setShowMove(true)
        else setShowStats(true)
      }
    }

    def resolveTargetDir(move: Boolean, path: String): Future[(String, Boolean)] = {
      val dirF = for {
        dir <- props.actions.readDir(Some(fromPath), path)
        sameDrive <-
          if (move) checkSameDrive(fromPath, dir.path)
          else Future.successful(false)
      } yield {
        (dir.path, sameDrive)
      }
      
      props.dispatch(FileListTaskAction(FutureTask("Resolving target dir", dirF)))
      dirF
    }

    val maybeError = maybeToPath.filter(_ => !inplace).flatMap { toPath =>
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
          move = isMove,
          path =
            if (!isInplace) toState.currDir.path
            else fromState.currentItem.map(_.name).getOrElse(""),
          items = items,
          onAction = onAction,
          onCancel = onCancel(dispatchAction = true)
        ))()
      }
      else if (maybeError.isDefined) maybeError.map { error =>
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Error",
          message = error,
          actions = List(MessageBoxAction.OK(onCancel(dispatchAction = false))),
          style = Theme.current.popup.error
        ))()
      }
      else if (showStats) Some {
        <(copyItemsStats())(^.wrapped := CopyItemsStatsProps(
          dispatch = props.dispatch,
          actions = props.actions,
          fromPath = fromPath,
          items = items,
          title = if (move) "Move" else "Copy",
          onDone = { total =>
            setTotal(Some(total))
            setShowStats(false)
          },
          onCancel = onCancel(dispatchAction = false)
        ))()
      }
      else if (showMove) maybeToPath.map { toPath =>
        <(moveProcessComp())(^.wrapped := MoveProcessProps(
          dispatch = props.dispatch,
          actions = props.actions,
          fromPath = fromPath,
          items =
            if (!inplace) items.map(i => (i, i.name))
            else items.map(i => (i, toPath)),
          toPath =
            if (!inplace) toPath
            else fromPath,
          onTopItem = onTopItem,
          onDone = onDone
        ))()
      }
      else {
        for {
          toPath <- maybeToPath
          total <- maybeTotal
        } yield {
          <(copyProcessComp())(^.wrapped := CopyProcessProps(
            dispatch = props.dispatch,
            actions = props.actions,
            move = move,
            fromPath = fromPath,
            items =
              if (!inplace) items.map(i => (i, i.name))
              else items.map(i => (i, toPath)),
            toPath =
              if (!inplace) toPath
              else fromPath,
            total = total,
            onTopItem = onTopItem,
            onDone = onDone
          ))()
        }
      }
    )
  }
  
  private def checkSameDrive(fromPath: String, toPath: String): Future[Boolean] = {
    for {
      maybeFromDisk <- fsService.readDisk(fromPath)
      maybeToDisk <- fsService.readDisk(toPath)
    } yield {
      val maybeSameDrive = for {
        fromDisk <- maybeFromDisk
        toDisk <- maybeToDisk
      } yield {
        fromDisk.root == toDisk.root
      }

      maybeSameDrive.getOrElse(false)
    }
  }
}
