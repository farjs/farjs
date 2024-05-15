package farjs.filelist.stack

import scala.scalajs.js

sealed trait PanelStacks extends js.Object {
  val left: PanelStackData
  val right: PanelStackData
}

object PanelStacks {

  def apply(left: PanelStackData,
            right: PanelStackData): PanelStacks = {

    js.Dynamic.literal(
      left = left,
      right = right
    ).asInstanceOf[PanelStacks]
  }

  def unapply(arg: PanelStacks): Option[(PanelStackData, PanelStackData)] = {
    Some((
      arg.left,
      arg.right
    ))
  }

  def active(stacks: PanelStacks): PanelStackData =
    if (stacks.left.stack.isActive) stacks.left
    else stacks.right

  def nonActive(stacks: PanelStacks): PanelStackData =
    if (!stacks.left.stack.isActive) stacks.left
    else stacks.right
}
