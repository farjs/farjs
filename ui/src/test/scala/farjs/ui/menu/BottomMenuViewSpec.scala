package farjs.ui.menu

import farjs.ui.theme.ThemeSpec.withThemeContext
import farjs.ui.theme.XTerm256Theme
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class BottomMenuViewSpec extends TestSpec with TestRendererUtils {

  it should "emit keypress event when onClick" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)
    
    val props = BottomMenuViewProps(width = 80, items = js.Array(List.fill(12)("item"): _*))
    val clickables = createTestRenderer(withThemeContext(<(BottomMenuView())(^.plain := props)()))
      .root.children

    //then
    inSequence {
      onKey.expects("f1", false, false, false)
      onKey.expects("f2", false, false, false)
      onKey.expects("f3", false, false, false)
      onKey.expects("f4", false, false, false)
      onKey.expects("f5", false, false, false)
      onKey.expects("f6", false, false, false)
      onKey.expects("f7", false, false, false)
      onKey.expects("f8", false, false, false)
      onKey.expects("f9", false, false, false)
      onKey.expects("f10", false, false, false)
      onKey.expects("f11", false, false, false)
      onKey.expects("f12", false, false, false)
    }

    //when
    clickables(0).props.onClick(null)
    clickables(1).props.onClick(null)
    clickables(2).props.onClick(null)
    clickables(3).props.onClick(null)
    clickables(4).props.onClick(null)
    clickables(5).props.onClick(null)
    clickables(6).props.onClick(null)
    clickables(7).props.onClick(null)
    clickables(8).props.onClick(null)
    clickables(9).props.onClick(null)
    clickables(10).props.onClick(null)
    clickables(11).props.onClick(null)
    
    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "render component" in {
    //given
    val currTheme = XTerm256Theme
    val theme = currTheme.menu
    val items = js.Array(List.fill(12)("item").updated(5, "longitem"): _*)
    val props = BottomMenuViewProps(width = 98, items = items)

    //when
    val result = testRender(withThemeContext(<(BottomMenuView())(^.plain := props)(), currTheme))

    //then
    val itemsWithPos = List(
      (1, " item ", 0, 8),
      (2, " item ", 8, 8),
      (3, " item ", 16, 8),
      (4, " item ", 24, 8),
      (5, " item ", 32, 8),
      (6, "longit", 40, 8),
      (7, " item ", 48, 8),
      (8, " item ", 56, 8),
      (9, " item ", 64, 8),
      (10, " item ", 72, 8),
      (11, " item ", 80, 8),
      (12, " item ", 88, 8)
    )
    val keyFg = theme.key.fg
    val keyBg = theme.key.bg
    val itemFg = theme.item.fg
    val itemBg = theme.item.bg

    result.children.zip(itemsWithPos).foreach {
      case (text, (num, item, pos, textWidth)) =>
        assertNativeComponent(text,
          <.text(
            ^.key := s"$num",
            ^.rbWidth := textWidth,
            ^.rbAutoFocus := false,
            ^.rbClickable := true,
            ^.rbTags := true,
            ^.rbMouse := true,
            ^.rbLeft := pos,
            ^.content := f"{$keyFg-fg}{$keyBg-bg}{bold}$num%2d{/}{$itemFg-fg}{$itemBg-bg}{bold}$item{/}"
          )()
        )
    }
  }
}
