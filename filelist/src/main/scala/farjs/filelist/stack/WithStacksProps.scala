package farjs.filelist.stack

import scala.scalajs.js

sealed trait WithStacksProps extends js.Object {
  val left: WithStacksData
  val right: WithStacksData
}

object WithStacksProps {

  def apply(left: WithStacksData,
            right: WithStacksData): WithStacksProps = {

    js.Dynamic.literal(
      left = left,
      right = right
    ).asInstanceOf[WithStacksProps]
  }

  def unapply(arg: WithStacksProps): Option[(WithStacksData, WithStacksData)] = {
    Some((
      arg.left,
      arg.right
    ))
  }

  def active(stacks: WithStacksProps): WithStacksData =
    if (stacks.left.stack.isActive) stacks.left
    else stacks.right

  def nonActive(stacks: WithStacksProps): WithStacksData =
    if (!stacks.left.stack.isActive) stacks.left
    else stacks.right
}
