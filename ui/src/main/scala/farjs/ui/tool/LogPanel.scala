package farjs.ui.tool

import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

object LogPanel extends FunctionComponent[LogPanelProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <.log(
      ^.rbAutoFocus := false,
      ^.rbMouse := true,
      ^.rbStyle := styles.container,
      ^.rbScrollbar := true,
      ^.rbScrollable := true,
      ^.rbAlwaysScroll := true,
      ^.content := props.content
    )()
  }
  
  private[tool] lazy val styles = new Styles
  private[tool] class Styles extends js.Object {
    
    val container: BlessedStyle = new BlessedStyle {
      override val scrollbar = new BlessedScrollBarStyle {
        override val bg = "cyan"
      }
    }
  }
}
