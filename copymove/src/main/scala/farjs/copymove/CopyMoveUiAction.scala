package farjs.copymove

sealed trait CopyMoveUiAction

object CopyMoveUiAction {

  case object ShowCopyToTarget extends CopyMoveUiAction
  case object ShowCopyInplace extends CopyMoveUiAction
  case object ShowMoveToTarget extends CopyMoveUiAction
  case object ShowMoveInplace extends CopyMoveUiAction
}
