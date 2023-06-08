package farjs.ui.tool

import scala.scalajs.js

sealed trait LogPanelProps extends js.Object {
  val content: String
}

object LogPanelProps {

  def apply(content: String): LogPanelProps = {
    js.Dynamic.literal(
      content = content
    ).asInstanceOf[LogPanelProps]
  }

  def unapply(arg: LogPanelProps): Option[String] = {
    Some(
      arg.content,
    )
  }
}
