package farjs.filelist.stack

import scommons.react.blessed.BlessedElement

import scala.scalajs.js

sealed trait PanelStackData extends js.Object {
  val stack: PanelStack
  val input: BlessedElement
}

object PanelStackData {

  def apply(stack: PanelStack,
            input: BlessedElement): PanelStackData = {

    js.Dynamic.literal(
      stack = stack,
      input = input
    ).asInstanceOf[PanelStackData]
  }

  def unapply(arg: PanelStackData): Option[(PanelStack, BlessedElement)] = {
    Some((
      arg.stack,
      arg.input
    ))
  }
}
