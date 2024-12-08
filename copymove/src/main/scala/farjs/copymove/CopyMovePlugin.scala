package farjs.copymove

import farjs.copymove.CopyMoveUiAction._
import farjs.filelist._
import farjs.filelist.api.{FileListCapability, FileListItem}
import farjs.filelist.stack.{PanelStack, WithStacksProps}
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement

import scala.scalajs.js

object CopyMovePlugin extends FileListPlugin(js.Array("f5", "f6", "S-f5", "S-f6")) {

  override def onKeyTrigger(key: String,
                            stacks: WithStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = {
    
    val (maybeFrom, maybeTo, toInput) =
      if (stacks.left.stack.isActive) {
        (getData(stacks.left.stack), getData(stacks.right.stack), stacks.right.input)
      }
      else (getData(stacks.right.stack), getData(stacks.left.stack), stacks.left.input)

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

    js.Promise.resolve[js.UndefOr[ReactClass]](res match {
      case Some(r) => r
      case None => js.undefined
    })
  }

  private def getData(stack: PanelStack): Option[FileListData] = {
    val item = stack.peek[js.Any]()
    item.getData().toOption
  }

  private[copymove] final def onCopyMoveInplace(move: Boolean, from: FileListData): Option[CopyMoveUiAction] = {
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

  private[copymove] final def onCopyMove(move: Boolean,
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
