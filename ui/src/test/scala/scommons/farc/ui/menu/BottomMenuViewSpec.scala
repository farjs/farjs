package scommons.farc.ui.menu

import org.scalatest.Succeeded
import scommons.farc.ui.menu.BottomMenuView.styles
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

class BottomMenuViewSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "not re-render component if the same props" in {
    //given
    val props = BottomMenuViewProps(width = 98, items = BottomMenu.items)
    val renderer = createTestRenderer(<(BottomMenuView())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children(12)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"
    val props2 = props.copy(items = BottomMenu.items)
    props2 shouldBe props
    props2 should not be theSameInstanceAs(props)

    //when
    renderer.update(<(BottomMenuView())(^.wrapped := props2)(
      <.text(^.content := "update")()
    ))

    //then
    val sameEl = renderer.root.children.head.children(12)
    sameEl.`type` shouldBe "text"
    sameEl.props.content shouldBe "initial"
  }
  
  it should "re-render component if different props" in {
    //given
    val props = BottomMenuViewProps(width = 98, items = BottomMenu.items)
    val renderer = createTestRenderer(<(BottomMenuView())(^.wrapped := props)(
      <.text(^.content := "initial")()
    ))
    val testEl = renderer.root.children.head.children(12)
    testEl.`type` shouldBe "text"
    testEl.props.content shouldBe "initial"

    //when
    renderer.update(<(BottomMenuView())(^.wrapped := props.copy(width = 95))(
      <.text(^.content := "update")()
    ))

    //then
    val updatedEl = renderer.root.children.head.children(12)
    updatedEl.`type` shouldBe "text"
    updatedEl.props.content shouldBe "update"
  }
  
  it should "render component" in {
    //given
    val props = BottomMenuViewProps(width = 98, items = BottomMenu.items)

    //when
    val result = shallowRender(<(BottomMenuView())(^.wrapped := props)())

    //then
    val itemsWithPos = List(
      (1, "      ", 0, 8),
      (2, "      ", 8, 8),
      (3, "      ", 16, 8),
      (4, "      ", 24, 8),
      (5, "      ", 32, 8),
      (6, "      ", 40, 8),
      (7, "      ", 48, 8),
      (8, "      ", 56, 8),
      (9, "      ", 64, 8),
      (10, " Exit ", 72, 8),
      (11, "      ", 80, 8),
      (12, "      ", 88, 8)
    )
    val keyFg = styles.key.fg
    val keyBg = styles.key.bg
    val itemFg = styles.item.fg
    val itemBg = styles.item.bg

    assertNativeComponent(result, <.>()(), { children: List[ShallowInstance] =>
      children.zip(itemsWithPos).foreach {
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
              ^.content := f"{$keyFg-fg}{$keyBg-bg}$num%2d{/}{$itemFg-fg}{$itemBg-bg}$item{/}"
            )()
          )
      }
      
      Succeeded
    })
  }
}
