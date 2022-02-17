package farjs.filelist.copy

import farjs.filelist.FileListActions.{FileListParamsChangedAction, FileListTaskAction}
import farjs.filelist.api.FileListItem
import farjs.filelist.fs.FSService
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.FileListPopupsState
import farjs.filelist.stack.{PanelStack, WithPanelStacks}
import farjs.filelist.{FileListActions, FileListState}
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs.path
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object CopyItems extends FunctionComponent[FileListPopupsState] {

  private[copy] var copyItemsStats: UiComponent[CopyItemsStatsProps] = CopyItemsStats
  private[copy] var copyItemsPopup: UiComponent[CopyItemsPopupProps] = CopyItemsPopup
  private[copy] var copyProcessComp: UiComponent[CopyProcessProps] = CopyProcess
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  private[copy] var moveProcessComp: UiComponent[MoveProcessProps] = MoveProcess
  private[copy] var fsService: FSService = FSService.instance

  private class Data(val dispatch: Dispatch,
                     val actions: FileListActions,
                     val state: FileListState) {
    
    def path: String = state.currDir.path
  }
  
  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (maybeTotal, setTotal) = useState[Option[Double]](None)
    val (maybeToPath, setToPath) = useState[Option[String]](None)
    val (inplace, setInplace) = useState(false)
    val (move, setMove) = useState(false)
    val (showStats, setShowStats) = useState(false)
    val (showMove, setShowMove) = useState(false)
    val copied = useRef(Set.empty[String])

    val showCopyMovePopup = compProps.wrapped.showCopyMovePopup
    
    def getData(stack: PanelStack): Option[Data] = {
      val item = stack.peek[FileListState]
      item.getActions.zip(item.state).map { case ((dispatch, actions), state) =>
        new Data(dispatch, actions, state)
      }
    }
    
    val (maybeFrom, maybeTo) =
      if (stacks.leftStack.isActive) (getData(stacks.leftStack), getData(stacks.rightStack))
      else (getData(stacks.rightStack), getData(stacks.leftStack))

    maybeFrom.map { from =>

      def onCancel(dispatchAction: Boolean): () => Unit = { () =>
        if (dispatchAction) {
          from.dispatch(FileListPopupCopyMoveAction(CopyMoveHidden))
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
        val updatedSelection = from.state.selectedNames -- copied.current
        if (updatedSelection != from.state.selectedNames) {
          from.dispatch(FileListParamsChangedAction(
            offset = from.state.offset,
            index = from.state.index,
            selectedNames = updatedSelection
          ))
        }

        onCancel(dispatchAction = false)()

        val updateAction = from.actions.updateDir(from.dispatch, from.path)
        from.dispatch(updateAction)
        updateAction.task.future.andThen {
          case Success(_) => maybeTo.foreach { to =>
            to.dispatch(to.actions.updateDir(to.dispatch, to.path))
          }
        }
      }

      def isMove: Boolean =
        move ||
          showCopyMovePopup == ShowMoveToTarget ||
          showCopyMovePopup == ShowMoveInplace

      def isInplace: Boolean = {
        inplace ||
          showCopyMovePopup == ShowCopyInplace ||
          showCopyMovePopup == ShowMoveInplace ||
          maybeTo.forall(_.path == from.path) && from.state.selectedItems.isEmpty
      }

      val items =
        if (!isInplace && from.state.selectedItems.nonEmpty) from.state.selectedItems
        else from.state.currentItem.toList

      def onAction(path: String): Unit = {
        val move = isMove
        val inplace = isInplace
        val resolveF =
          if (!inplace) resolveTargetDir(move, path)
          else Future.successful((path, true))

        resolveF.map { case (toPath, sameDrive) =>
          setInplace(inplace)
          setMove(move)
          from.dispatch(FileListPopupCopyMoveAction(CopyMoveHidden))

          setToPath(Some(toPath))
          if (move && sameDrive) setShowMove(true)
          else setShowStats(true)
        }
      }

      def resolveTargetDir(move: Boolean, path: String): Future[(String, Boolean)] = {
        val dirF = for {
          dir <- from.actions.readDir(Some(from.path), path)
          sameDrive <-
            if (move) checkSameDrive(from.path, dir.path)
            else Future.successful(false)
        } yield {
          (dir.path, sameDrive)
        }

        from.dispatch(FileListTaskAction(FutureTask("Resolving target dir", dirF)))
        dirF
      }

      val maybeError = maybeToPath.filter(_ => !inplace).flatMap { toPath =>
        val op = if (move) "move" else "copy"
        if (from.path == toPath) Some {
          s"Cannot $op the item\n${items.head.name}\nonto itself"
        }
        else if (toPath.startsWith(from.path + path.sep)) {
          val toSuffix = toPath.stripPrefix(from.path + path.sep)
          val maybeSelf = items.find(i => toSuffix == i.name || toSuffix.startsWith(i.name + path.sep))
          maybeSelf.map { self =>
            s"Cannot $op the item\n${self.name}\ninto itself"
          }
        }
        else None
      }

      <.>()(
        if (showCopyMovePopup != CopyMoveHidden) Some {
          <(copyItemsPopup())(^.wrapped := CopyItemsPopupProps(
            move = isMove,
            path =
              if (!isInplace) maybeTo.map(_.path).getOrElse("")
              else from.state.currentItem.map(_.name).getOrElse(""),
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
            dispatch = from.dispatch,
            actions = from.actions,
            fromPath = from.path,
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
            dispatch = from.dispatch,
            actions = from.actions,
            fromPath = from.path,
            items =
              if (!inplace) items.map(i => (i, i.name))
              else items.map(i => (i, toPath)),
            toPath =
              if (!inplace) toPath
              else from.path,
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
              dispatch = from.dispatch,
              actions = from.actions,
              move = move,
              fromPath = from.path,
              items =
                if (!inplace) items.map(i => (i, i.name))
                else items.map(i => (i, toPath)),
              toPath =
                if (!inplace) toPath
                else from.path,
              total = total,
              onTopItem = onTopItem,
              onDone = onDone
            ))()
          }
        }
      )
    }.orNull
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
