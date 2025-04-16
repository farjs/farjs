package farjs.archiver

import scala.scalajs.js

sealed trait AddToArchAction extends js.Object

object AddToArchAction {

  val Add: AddToArchAction = "Add".asInstanceOf[AddToArchAction]
  val Copy: AddToArchAction = "Copy".asInstanceOf[AddToArchAction]
  val Move: AddToArchAction = "Move".asInstanceOf[AddToArchAction]
}
