package farjs.copymove

import farjs.copymove.CopyMoveUiAction._
import farjs.filelist._
import farjs.filelist.api.{FileListCapability, FileListItem}
import farjs.filelist.stack.{PanelStack, WithPanelStacksProps}
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement

import scala.concurrent.Future
import scala.scalajs.js

object CopyMovePlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("f5", "f6", "S-f5", "S-f6")

  override def onKeyTrigger(key: String,
                            stacks: WithPanelStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Future[Option[ReactClass]] = {
    
    val (maybeFrom, maybeTo, toInput) =
      if (stacks.leftStack.isActive) {
        (getData(stacks.leftStack), getData(stacks.rightStack), stacks.rightInput)
      }
      else (getData(stacks.rightStack), getData(stacks.leftStack), stacks.leftInput)

    val res = key match {
      case "f5" | "f6" =>
        maybeFrom.zip(maybeTo).flatMap { case (from, to) =>
          onCopyMove(key == "f6", from, to, toInput).map { action =>
            new CopyMoveUi(action, from, Some(to)).apply()
          }
        }
      case "S-f5" | "S-f6" =>
        maybeFrom.flatMap { from =>
          onCopyMoveInplace(key == "S-f6", from).map { action =>
            new CopyMoveUi(action, from, None).apply()
          }
        }
      case _ => None
    }
    Future.successful(res)
  }

  private def getData(stack: PanelStack): Option[FileListData] = {
    val item = stack.peek[js.Any]
    item.getData.toOption
  }

  private[copymove] def onCopyMoveInplace(move: Boolean, from: FileListData): Option[CopyMoveUiAction] = {
    FileListState.currentItem(from.state).filter(_ != FileListItem.up).toOption.flatMap { _ =>
      if (move && from.actions.api.capabilities.contains(FileListCapability.moveInplace)) {
        Some(ShowMoveInplace)
      }
      else if (!move && from.actions.api.capabilities.contains(FileListCapability.copyInplace)) {
        Some(ShowCopyInplace)
      }
      else None
    }
  }

  private[copymove] def onCopyMove(move: Boolean,
                                   from: FileListData,
                                   to: FileListData,
                                   toInput: BlessedElement): Option[CopyMoveUiAction] = {

    val currItem = FileListState.currentItem(from.state).filter(_ != FileListItem.up)

    if ((from.state.selectedNames.nonEmpty || currItem.nonEmpty) &&
      from.actions.api.capabilities.contains(FileListCapability.read) &&
      (!move || from.actions.api.capabilities.contains(FileListCapability.delete))) {

      if (to.actions.api.capabilities.contains(FileListCapability.write)) {
        if (move) Some(ShowMoveToTarget)
        else Some(ShowCopyToTarget)
      }
      else {
        toInput.emit("keypress", js.undefined, js.Dynamic.literal(
          name = "",
          full =
            if (move) FileListEvent.onFileListMove
            else FileListEvent.onFileListCopy
        ))
        None
      }
    }
    else None
  }
}
