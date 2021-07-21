package farjs.app.util

sealed abstract class DevTool(next: => DevTool) {

  def getNext: DevTool = next
}

object DevTool {

  def shouldResize(from: DevTool, to: DevTool): Boolean = {
    from == Hidden || to == Hidden
  }

  object Hidden extends DevTool(Logs)
  object Logs extends DevTool(Inputs)
  object Inputs extends DevTool(Colors)
  object Colors extends DevTool(Hidden)
}
