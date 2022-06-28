package farjs.filelist.sort

import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class SortIndicatorProps(mode: SortMode, ascending: Boolean)

object SortIndicator extends FunctionComponent[SortIndicatorProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val text = s"${getIndicator(props.mode, props.ascending)} "
    val theme = Theme.current.fileList

    <.text(
      ^.rbWidth := text.length,
      ^.rbHeight := 1,
      ^.rbLeft := 1,
      ^.rbTop := 1,
      ^.rbStyle := theme.header,
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
