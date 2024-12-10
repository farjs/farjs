package farjs.filelist.stack

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait WithStacksProps extends js.Object {
  val left: WithStacksData
  val right: WithStacksData
}

@js.native
@JSImport("@farjs/filelist/stack/WithStacksProps.mjs", JSImport.Default)
object NativeWithStacksProps extends js.Function2[WithStacksData, WithStacksData, WithStacksProps] {

  def apply(left: WithStacksData, right: WithStacksData): WithStacksProps = js.native

  def active(stacks: WithStacksProps): WithStacksData = js.native

  def nonActive(stacks: WithStacksProps): WithStacksData = js.native
}

object WithStacksProps {

  def apply(left: WithStacksData,
            right: WithStacksData): WithStacksProps = {

    NativeWithStacksProps(left, right)
  }

  def unapply(arg: WithStacksProps): Option[(WithStacksData, WithStacksData)] = {
    Some((
      arg.left,
      arg.right
    ))
  }

  def active(stacks: WithStacksProps): WithStacksData =
    NativeWithStacksProps.active(stacks)

  def nonActive(stacks: WithStacksProps): WithStacksData =
    NativeWithStacksProps.nonActive(stacks)
}
