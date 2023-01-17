package farjs.copymove

import farjs.copymove.CopyMoveUiAction._
import farjs.filelist._
import farjs.filelist.api.{FileListCapability, FileListItem}
import farjs.filelist.stack.{PanelStack, WithPanelStacksProps}
import scommons.react.ReactClass
import scommons.react.blessed.BlessedElement

import scala.scalajs.js

object CopyMovePlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("f5", "f6", "S-f5", "S-f6")

  override def onKeyTrigger(key: String, stacks: WithPanelStacksProps): Option[ReactClass] = {
    val (stack, nonActiveStack, nonActiveInput) =
      if (stacks.leftStack.isActive) (stacks.leftStack, stacks.rightStack, stacks.rightInput)
      else (stacks.rightStack, stacks.leftStack, stacks.leftInput)

    key match {
      case "f5" | "f6" =>
        onCopyMove(key == "f6", stack, nonActiveStack, nonActiveInput).map { uiAction =>
          new CopyMoveUi(uiAction).apply()
        }
      case "S-f5" | "S-f6" =>
        onCopyMoveInplace(key == "S-f6", stack).map { uiAction =>
          new CopyMoveUi(uiAction).apply()
        }
      case _ => None
    }
  }

  private[copymove] def onCopyMoveInplace(move: Boolean, stack: PanelStack): Option[CopyMoveUiAction] = {
    val stackItem = stack.peek[js.Any]
    stackItem.getActions.zip(stackItem.state).collect {
      case ((_, actions), state: FileListState) => (actions, state)
    }.flatMap { case (actions, state) =>
      state.currentItem.filter(_ != FileListItem.up).flatMap { _ =>
        if (move && actions.capabilities.contains(FileListCapability.moveInplace)) {
          Some(ShowMoveInplace)
        }
        else if (!move && actions.capabilities.contains(FileListCapability.copyInplace)) {
          Some(ShowCopyInplace)
        }
        else None
      }
    }
  }

  private[copymove] def onCopyMove(move: Boolean,
                                   stack: PanelStack,
                                   nonActiveStack: PanelStack,
                                   nonActiveInput: BlessedElement): Option[CopyMoveUiAction] = {

    val nonActiveItem = nonActiveStack.peek[js.Any]
    val stackItem = stack.peek[js.Any]
    stackItem.getActions.zip(stackItem.state).zip(nonActiveItem.getActions).collect {
      case (((_, actions), state: FileListState), (_, nonActiveActions)) =>
        (actions, state, nonActiveActions)
    }.flatMap { case (actions, state, nonActiveActions) =>
      val currItem = state.currentItem.filter(_ != FileListItem.up)
      if ((state.selectedNames.nonEmpty || currItem.nonEmpty) &&
        actions.capabilities.contains(FileListCapability.read) &&
        (!move || actions.capabilities.contains(FileListCapability.delete))) {

        if (nonActiveActions.capabilities.contains(FileListCapability.write)) {
          if (move) Some(ShowMoveToTarget)
          else Some(ShowCopyToTarget)
        }
        else {
          nonActiveInput.emit("keypress", js.undefined, js.Dynamic.literal(
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
}
