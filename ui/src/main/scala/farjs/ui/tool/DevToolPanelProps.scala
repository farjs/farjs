package farjs.ui.tool

import scala.scalajs.js

sealed trait DevToolPanelProps extends js.Object {
  val devTool: DevTool
  val logContent: String
  val onActivate: js.Function1[DevTool, Unit]
}

object DevToolPanelProps {

  def apply(devTool: DevTool,
            logContent: String,
            onActivate: js.Function1[DevTool, Unit]): DevToolPanelProps = {

    js.Dynamic.literal(
      devTool = devTool,
      logContent = logContent,
      onActivate = onActivate
    ).asInstanceOf[DevToolPanelProps]
  }

  def unapply(arg: DevToolPanelProps): Option[(DevTool, String, js.Function1[DevTool, Unit])] = {
    Some((
      arg.devTool,
      arg.logContent,
      arg.onActivate
    ))
  }
}
