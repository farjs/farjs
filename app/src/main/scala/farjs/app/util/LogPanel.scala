package farjs.app.util

import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class LogPanelProps(content: String)

object LogPanel extends FunctionComponent[LogPanelProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
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
  
  private[util] lazy val styles = new Styles
  private[util] class Styles extends js.Object {
    
    val container: BlessedStyle = new BlessedStyle {
      override val scrollbar = new BlessedScrollBarStyle {
        override val bg = "cyan"
      }
    }
  }
}
