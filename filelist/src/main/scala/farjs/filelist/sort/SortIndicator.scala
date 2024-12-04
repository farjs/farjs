package farjs.filelist.sort

import farjs.filelist.stack.PanelStackComp
import farjs.filelist.theme.FileListTheme
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

object SortIndicator extends FunctionComponent[SortIndicatorProps] {

  protected def render(compProps: Props): ReactElement = {
    val stackProps = PanelStackComp.usePanelStack
    val props = compProps.plain
    val text = s"${getIndicator(props.sort)} "
    val theme = FileListTheme.useTheme().fileList

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

  private[sort] def getIndicator(sort: FileListSort): String = {
    val indicator = sort.mode match {
      case SortMode.Name => "n"
      case SortMode.Extension => "x"
      case SortMode.ModificationTime => "m"
      case SortMode.Size => "s"
      case SortMode.Unsorted => "u"
      case SortMode.CreationTime => "c"
      case SortMode.AccessTime => "a"
    }
    
    if (sort.asc) indicator
    else indicator.toUpperCase
  }
}
