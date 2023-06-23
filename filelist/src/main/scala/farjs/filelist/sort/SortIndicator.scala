package farjs.filelist.sort

import farjs.filelist.stack.PanelStack
import farjs.filelist.theme.FileListTheme
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class SortIndicatorProps(mode: SortMode, ascending: Boolean)

object SortIndicator extends FunctionComponent[SortIndicatorProps] {

  protected def render(compProps: Props): ReactElement = {
    val stackProps = PanelStack.usePanelStack
    val props = compProps.wrapped
    val text = s"${getIndicator(props.mode, props.ascending)} "
    val theme = FileListTheme.useTheme.fileList

    <.text(
      ^.rbWidth := text.length,
      ^.rbHeight := 1,
      ^.rbLeft := 1,
      ^.rbTop := 1,
      ^.rbAutoFocus := false,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbStyle := theme.header,
      ^.rbOnClick := { _ =>
        process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
          name =
            if (stackProps.isRight) "r"
            else "l",
          ctrl = false,
          meta = true,
          shift = false
        ))
      },
      ^.content := text
    )()
  }

  private[sort] def getIndicator(mode: SortMode, ascending: Boolean): String = {
    val indicator = mode match {
      case SortMode.Name => "n"
      case SortMode.Extension => "x"
      case SortMode.ModificationTime => "m"
      case SortMode.Size => "s"
      case SortMode.Unsorted => "u"
      case SortMode.CreationTime => "c"
      case SortMode.AccessTime => "a"
    }
    
    if (ascending) indicator
    else indicator.toUpperCase
  }
}
