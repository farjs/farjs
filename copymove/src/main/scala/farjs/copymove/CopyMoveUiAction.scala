package farjs.copymove

import scala.scalajs.js

sealed trait CopyMoveUiAction extends js.Object

object CopyMoveUiAction {

  val ShowCopyToTarget: CopyMoveUiAction = "ShowCopyToTarget".asInstanceOf[CopyMoveUiAction]
  val ShowCopyInplace: CopyMoveUiAction = "ShowCopyInplace".asInstanceOf[CopyMoveUiAction]
  val ShowMoveToTarget: CopyMoveUiAction = "ShowMoveToTarget".asInstanceOf[CopyMoveUiAction]
  val ShowMoveInplace: CopyMoveUiAction = "ShowMoveInplace".asInstanceOf[CopyMoveUiAction]
}
