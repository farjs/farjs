package farjs.filelist.stack

import scommons.react.blessed.BlessedElement

import scala.scalajs.js

sealed trait WithStacksData extends js.Object {
  val stack: PanelStack
  val input: BlessedElement
}

object WithStacksData {

  def apply(stack: PanelStack,
            input: BlessedElement): WithStacksData = {

    js.Dynamic.literal(
      stack = stack,
      input = input
    ).asInstanceOf[WithStacksData]
  }

  def unapply(arg: WithStacksData): Option[(PanelStack, BlessedElement)] = {
    Some((
      arg.stack,
      arg.input
    ))
  }
}
