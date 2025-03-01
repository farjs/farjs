package farjs.viewer

import scala.scalajs.js

sealed trait ViewerFileLine extends js.Object {
  val line: String
  val bytes: Int
}

object ViewerFileLine {

  def apply(line: String, bytes: Int): ViewerFileLine = {
    js.Dynamic.literal(
      line = line,
      bytes = bytes
    ).asInstanceOf[ViewerFileLine]
  }

  def unapply(arg: ViewerFileLine): Option[(String, Int)] = {
    Some((
      arg.line,
      arg.bytes
    ))
  }
}
