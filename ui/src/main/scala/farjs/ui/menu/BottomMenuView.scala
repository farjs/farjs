package farjs.ui.menu

import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class BottomMenuViewProps(width: Int, items: List[String])

object BottomMenuView extends FunctionComponent[BottomMenuViewProps] {

  override protected def create(): ReactClass = {
    ReactMemo[Props](super.create(), { (prevProps, nextProps) =>
      prevProps.wrapped == nextProps.wrapped
    })
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.useTheme().menu
    val width = props.width
    val items = props.items

    val itemsCount = items.size
    val itemWidth = width / itemsCount
    val itemsWithPos = items.zipWithIndex.map { case (item, index) =>
      val leftPos = index * itemWidth

      val text = {
        val textWidth = math.max(itemWidth - 2, 0) // excluding key width
        val len = item.length
        if (len < textWidth) {
          val pad = (textWidth - len) / 2
          val paddingL = " " * pad
          val paddingR = " " * {
            if ((pad + len + pad) < textWidth) textWidth - pad - len
            else pad
          }
          
          s"$paddingL$item$paddingR"
        }
        else item.take(textWidth)
      }
      
      (index + 1, text, leftPos, itemWidth)
    }
    
    val keyBold = if (theme.key.bold.getOrElse(false)) "{bold}" else ""
    val keyFg = theme.key.fg
    val keyBg = theme.key.bg
    
    val itemBold = if (theme.item.bold.getOrElse(false)) "{bold}" else ""
    val itemFg = theme.item.fg
    val itemBg = theme.item.bg
    
    <.>()(
      itemsWithPos.map { case (key, item, pos, textWidth) =>
        <.text(
          ^.key := s"$key",
          ^.rbWidth := textWidth,
          ^.rbAutoFocus := false,
          ^.rbClickable := true,
          ^.rbTags := true,
          ^.rbMouse := true,
          ^.rbLeft := pos,
          ^.rbOnClick := { _ =>
            process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
              name = s"f$key",
              ctrl = false,
              meta = false,
              shift = false
            ))
          },
          ^.content := f"{$keyFg-fg}{$keyBg-bg}$keyBold$key%2d{/}{$itemFg-fg}{$itemBg-bg}$itemBold$item{/}"
        )()
      },

      compProps.children // just for testing memo/re-render
    )
  }
}
