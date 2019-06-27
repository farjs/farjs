package scommons.farc.ui.menu

import scommons.react._
import scommons.react.blessed._

case class BottomMenuViewProps(width: Int, items: List[String])

object BottomMenuView extends FunctionComponent[BottomMenuViewProps] {

  override protected def create(): ReactClass = {
    ReactMemo[Props](super.create(), { (prevProps, nextProps) =>
      prevProps.wrapped == nextProps.wrapped
    })
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
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
    
    val keyFg = styles.key.fg
    val keyBg = styles.key.bg
    val itemFg = styles.item.fg
    val itemBg = styles.item.bg
    
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
            println(s"$key")
          },
          ^.content := f"{$keyFg-fg}{$keyBg-bg}$key%2d{/}{$itemFg-fg}{$itemBg-bg}$item{/}"
        )()
      },

      compProps.children // just for testing memo/re-render
    )
  }

  private[menu] lazy val styles = Styles

  private[menu] object Styles {

    val key = new BlessedStyle {
      override val bg = "black"
      override val fg = "white"
    }
    val item = new BlessedStyle {
      override val bg = "cyan"
      override val fg = "black"
    }
  }
}
