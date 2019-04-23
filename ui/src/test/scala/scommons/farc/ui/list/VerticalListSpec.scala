package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.TestRenderer
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js

class VerticalListSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "select item when onClick" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    root.children(0).props.style shouldBe VerticalList.styles.selectedItem

    //when & then
    TestRenderer.act { () =>
      root.children(1).props.onClick()
    }
    root.children(0).props.style shouldBe VerticalList.styles.normalItem
    root.children(1).props.style shouldBe VerticalList.styles.selectedItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(2).props.onClick()
    }
    root.children(1).props.style shouldBe VerticalList.styles.normalItem
    root.children(2).props.style shouldBe VerticalList.styles.selectedItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(2).props.onClick()
    }
    root.children(1).props.style shouldBe VerticalList.styles.normalItem
    root.children(2).props.style shouldBe VerticalList.styles.selectedItem
  }

  it should "select item when onKeypress" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    root.children(0).props.style shouldBe VerticalList.styles.selectedItem

    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "up"))
    }
    root.children(0).props.style shouldBe VerticalList.styles.selectedItem
    root.children(1).props.style shouldBe VerticalList.styles.normalItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "down"))
    }
    root.children(0).props.style shouldBe VerticalList.styles.normalItem
    root.children(1).props.style shouldBe VerticalList.styles.selectedItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "down"))
    }
    root.children(1).props.style shouldBe VerticalList.styles.normalItem
    root.children(2).props.style shouldBe VerticalList.styles.selectedItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "down"))
    }
    root.children(1).props.style shouldBe VerticalList.styles.normalItem
    root.children(2).props.style shouldBe VerticalList.styles.selectedItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "up"))
    }
    root.children(1).props.style shouldBe VerticalList.styles.selectedItem
    root.children(2).props.style shouldBe VerticalList.styles.normalItem
    
    //when & then
    TestRenderer.act { () =>
      root.children(0).props.onKeypress(null, js.Dynamic.literal(name = "unknown"))
    }
    root.children(1).props.style shouldBe VerticalList.styles.selectedItem
    root.children(2).props.style shouldBe VerticalList.styles.normalItem
  }

  it should "render component" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val comp = <(VerticalList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result,
      <.>()(
        props.items.zipWithIndex.map { case (text, index) =>
          val isSelected = index == 0
          
          <.button(
            ^.key := s"$index",
            ^.rbTop := index,
            ^.rbHeight := 1,
            ^.rbStyle := {
              if (isSelected) VerticalList.styles.selectedItem
              else VerticalList.styles.normalItem
            },
            ^.rbMouse := true,
            ^.content := text
          )()
        }
      )
    )
  }
}
