package farjs.ui

import scala.scalajs.js

sealed trait TextInputState extends js.Object {
  val offset: Int
  val cursorX: Int
  val selStart: Int
  val selEnd: Int
}

object TextInputState {

  def apply(offset: Int = 0,
            cursorX: Int = -1,
            selStart: Int = -1,
            selEnd: Int = -1): TextInputState = {

    js.Dynamic.literal(
      offset = offset,
      cursorX = cursorX,
      selStart = selStart,
      selEnd = selEnd
    ).asInstanceOf[TextInputState]
  }

  def unapply(arg: TextInputState): Option[(Int, Int, Int, Int)] = {
    Some((
      arg.offset,
      arg.cursorX,
      arg.selStart,
      arg.selEnd
    ))
  }

  def copy(p: TextInputState)(offset: Int = p.offset,
                              cursorX: Int = p.cursorX,
                              selStart: Int = p.selStart,
                              selEnd: Int = p.selEnd): TextInputState = {

    TextInputState(
      offset = offset,
      cursorX = cursorX,
      selStart = selStart,
      selEnd = selEnd
    )
  }
}
