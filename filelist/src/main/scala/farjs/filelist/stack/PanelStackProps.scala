package farjs.filelist.stack

import scommons.react.blessed.BlessedElement

import scala.scalajs.js

sealed trait PanelStackProps extends js.Object {
  val isRight: Boolean
  val panelInput: BlessedElement
  val stack: PanelStack
  val width: Int
  val height: Int
}

object PanelStackProps {

  def apply(isRight: Boolean,
            panelInput: BlessedElement,
            stack: PanelStack,
            width: Int = 0,
            height: Int = 0): PanelStackProps = {

    js.Dynamic.literal(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    ).asInstanceOf[PanelStackProps]
  }

  def unapply(arg: PanelStackProps): Option[(Boolean, BlessedElement, PanelStack, Int, Int)] = {
    Some((
      arg.isRight,
      arg.panelInput,
      arg.stack,
      arg.width,
      arg.height
    ))
  }

  def copy(p: PanelStackProps)(isRight: Boolean = p.isRight,
                               panelInput: BlessedElement = p.panelInput,
                               stack: PanelStack = p.stack,
                               width: Int = p.width,
                               height: Int = p.height): PanelStackProps = {

    PanelStackProps(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    )
  }
}
