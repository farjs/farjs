package farjs.ui.menu

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
    
    val keyBold = if (styles.key.bold.getOrElse(false)) "{bold}" else ""
    val keyFg = styles.key.fg
    val keyBg = styles.key.bg
    
    val itemBold = if (styles.item.bold.getOrElse(false)) "{bold}" else ""
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

  private[menu] lazy val styles = new Styles
  private[menu] class Styles {

    val key: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "black"
      override val fg = "#aaa"
    }
    val item: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = "#088"
      override val fg = "black"
    }
  }
}
