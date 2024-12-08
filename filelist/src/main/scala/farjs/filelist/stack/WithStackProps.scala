package farjs.filelist.stack

import scommons.react.blessed.BlessedElement

import scala.scalajs.js

sealed trait WithStackProps extends js.Object {
  val isRight: Boolean
  val panelInput: BlessedElement
  val stack: PanelStack
  val width: Int
  val height: Int
}

object WithStackProps {

  def apply(isRight: Boolean,
            panelInput: BlessedElement,
            stack: PanelStack,
            width: Int = 0,
            height: Int = 0): WithStackProps = {

    js.Dynamic.literal(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    ).asInstanceOf[WithStackProps]
  }

  def unapply(arg: WithStackProps): Option[(Boolean, BlessedElement, PanelStack, Int, Int)] = {
    Some((
      arg.isRight,
      arg.panelInput,
      arg.stack,
      arg.width,
      arg.height
    ))
  }

  def copy(p: WithStackProps)(isRight: Boolean = p.isRight,
                              panelInput: BlessedElement = p.panelInput,
                              stack: PanelStack = p.stack,
                              width: Int = p.width,
                              height: Int = p.height): WithStackProps = {

    WithStackProps(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    )
  }
}
