package farjs.ui.tool

import scala.scalajs.js

@js.native
sealed trait DevTool extends js.Object

object DevTool {

  def shouldResize(from: DevTool, to: DevTool): Boolean = {
    from == Hidden || to == Hidden
  }

  def getNext(from: DevTool): DevTool = from match {
    case Hidden => Logs
    case Logs => Inputs
    case Inputs => Colors
    case Colors => Hidden
  }

  val Hidden: DevTool = "Hidden".asInstanceOf[DevTool]
  val Logs: DevTool = "Logs".asInstanceOf[DevTool]
  val Inputs: DevTool = "Inputs".asInstanceOf[DevTool]
  val Colors: DevTool = "Colors".asInstanceOf[DevTool]
}
