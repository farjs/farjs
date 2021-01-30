package farjs.app.util

import farjs.ui.UI
import scommons.react._
import scommons.react.blessed._

object ColorPanel extends FunctionComponent[Unit] {
  
  protected def render(props: Props): ReactElement = {
    
    val levels = List(0x0, 0x5, 0x8, 0xa, 0xc, 0xe)
    val mainColors = levels.map { group =>
      levels.map { row =>
        levels.map { level =>
          val fg =
            if (group > 0xa) "black"
            else if (row > 0xa) "black"
            else if (level > 0xa) "black"
            else "white"

          val color = f"#$group%x$row%x$level%x"
          s"{$fg-fg}{$color-bg}$color{/}"
        }.mkString("")
      }.mkString(UI.newLine)
    }.mkString(UI.newLine)

    val grayScale = List(0x0, 0x3, 0x5, 0x8, 0xa, 0xc, 0xf).map { level =>
      val fg =
        if (level > 0xa) "black"
        else "white"
      
      val color = f"#$level%x$level%x$level%x"
      s"{$fg-fg}{$color-bg}$color{/}"
    }.mkString("")

    <.log(
      ^.rbAutoFocus := false,
      ^.rbMouse := true,
      ^.rbTags := true,
      ^.rbScrollbar := true,
      ^.rbScrollable := true,
      ^.rbAlwaysScroll := true,
      ^.content := s"$mainColors${UI.newLine}${UI.newLine}$grayScale"
    )()
  }
}
