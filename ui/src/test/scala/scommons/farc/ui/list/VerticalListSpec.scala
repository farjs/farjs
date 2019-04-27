package scommons.farc.ui.list

import org.scalatest.Succeeded
import scommons.farc.ui.list.VerticalListSpec._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.{ShallowInstance, TestRenderer}
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js.annotation.JSExportAll

class VerticalListSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "focus item when onFocus" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    findProps(root, ListItem)(1).focused shouldBe false

    //when & then
    TestRenderer.act { () =>
      findProps(root, ListItem)(1).onFocus()
    }
    findProps(root, ListItem)(1).focused shouldBe true
    
    //when & then
    TestRenderer.act { () =>
      findProps(root, ListItem)(2).onFocus()
    }
    findProps(root, ListItem)(1).focused shouldBe false
    findProps(root, ListItem)(2).focused shouldBe true
    
    //when & then
    TestRenderer.act { () =>
      findProps(root, ListItem)(2).onFocus()
    }
    findProps(root, ListItem)(1).focused shouldBe false
    findProps(root, ListItem)(2).focused shouldBe true
  }

  it should "do nothing when onKeyPress(up) on first item" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    TestRenderer.act { () =>
      findProps(root, ListItem).head.onFocus()
    }
    findProps(root, ListItem).head.focused shouldBe true

    val screenMock = mock[BlessedScreenMock]
    val elementMock = mock[BlessedElementMock]
    val keyMock = mock[KeyboardKeyMock]

    //then
    (elementMock.screen _).expects().never()
    (screenMock.focusOffset _).expects(*).never()
    (keyMock.full _).expects().returning("up")

    //when
    TestRenderer.act { () =>
      findProps(root, ListItem).head.onKeyPress(
        elementMock.asInstanceOf[BlessedElement],
        keyMock.asInstanceOf[KeyboardKey]
      )
    }
  }

  it should "do nothing when onKeyPress(down) on last item" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    TestRenderer.act { () =>
      findProps(root, ListItem).last.onFocus()
    }
    findProps(root, ListItem).last.focused shouldBe true

    val screenMock = mock[BlessedScreenMock]
    val elementMock = mock[BlessedElementMock]
    val keyMock = mock[KeyboardKeyMock]

    //then
    (elementMock.screen _).expects().never()
    (screenMock.focusOffset _).expects(*).never()
    (keyMock.full _).expects().returning("down")

    //when
    TestRenderer.act { () =>
      findProps(root, ListItem).last.onKeyPress(
        elementMock.asInstanceOf[BlessedElement],
        keyMock.asInstanceOf[KeyboardKey]
      )
    }
  }

  it should "focus previous item when onKeyPress(up)" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    TestRenderer.act { () =>
      findProps(root, ListItem)(1).onFocus()
    }
    findProps(root, ListItem)(1).focused shouldBe true

    val screenMock = mock[BlessedScreenMock]
    val elementMock = mock[BlessedElementMock]
    val keyMock = mock[KeyboardKeyMock]

    //then
    (elementMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focusOffset _).expects(-1)
    (keyMock.full _).expects().returning("up")

    //when
    TestRenderer.act { () =>
      findProps(root, ListItem)(1).onKeyPress(
        elementMock.asInstanceOf[BlessedElement],
        keyMock.asInstanceOf[KeyboardKey]
      )
    }
  }

  it should "focus next item when onKeyPress(down)" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val root = createTestRenderer(<(VerticalList())(^.wrapped := props)()).root
    TestRenderer.act { () =>
      findProps(root, ListItem)(1).onFocus()
    }
    findProps(root, ListItem)(1).focused shouldBe true

    val screenMock = mock[BlessedScreenMock]
    val elementMock = mock[BlessedElementMock]
    val keyMock = mock[KeyboardKeyMock]

    //then
    (elementMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focusOffset _).expects(1)
    (keyMock.full _).expects().returning("down")

    //when
    TestRenderer.act { () =>
      findProps(root, ListItem)(1).onKeyPress(
        elementMock.asInstanceOf[BlessedElement],
        keyMock.asInstanceOf[KeyboardKey]
      )
    }
  }

  it should "render component" in {
    //given
    val props = VerticalListProps(List("item 1", "item 2", "item 3"))
    val comp = <(VerticalList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result, <.>()(), { items: List[ShallowInstance] =>
      items.zipWithIndex.map { case (item, index) =>
        item.key shouldBe s"$index"
        assertComponent(item, ListItem) { case ListItemProps(pos, style, text, focused, _, _) =>
          pos shouldBe index
          style shouldBe VerticalList.styles.normalItem
          text shouldBe props.items(index)
          focused shouldBe false
        }
      }
      
      Succeeded
    })
  }
}

object VerticalListSpec {

  @JSExportAll
  trait KeyboardKeyMock {

    def full: String
  }

  @JSExportAll
  trait BlessedScreenMock {

    def focusOffset(offset: Int): Unit
  }

  @JSExportAll
  trait BlessedElementMock {

    def screen: BlessedScreen
  }
}
