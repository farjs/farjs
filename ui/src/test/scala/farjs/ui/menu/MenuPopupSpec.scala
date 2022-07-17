package farjs.ui.menu

import farjs.ui._
import farjs.ui.menu.MenuPopup._
import farjs.ui.popup.{ModalContentProps, PopupProps}
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.react.test._

class MenuPopupSpec extends TestSpec with TestRendererUtils {

  MenuPopup.popupComp = mockUiComponent("Popup")
  MenuPopup.modalContentComp = mockUiComponent("ModalContent")
  MenuPopup.buttonComp = mockUiComponent("Button")

  it should "call onSelect when onPress item" in {
    //given
    val onSelect = mockFunction[Int, Unit]
    val props = MenuPopupProps(
      title = "Test title",
      items = List("item 1", "item 2"),
      getLeft = w => s"$w",
      onSelect = onSelect,
      onClose = () => ()
    )
    val comp = testRender(<(MenuPopup())(^.wrapped := props)())
    val button2 = inside(findProps(comp, buttonComp, plain = true)) {
      case List(_, b2) => b2
    }
    
    //then
    onSelect.expects(1)
    
    //when
    button2.onPress()
  }

  it should "render component" in {
    //given
    val props = MenuPopupProps(
      title = "Test title",
      items = List("item 1", "item 2"),
      getLeft = w => s"$w",
      onSelect = _ => (),
      onClose = () => ()
    )
    
    //when
    val result = testRender(<(MenuPopup())(^.wrapped := props)())
    
    //then
    assertMenuPopup(result, props)
  }

  it should "calculate and return left pos when getLeftPos" in {
    //when & then
    getLeftPos(10, showOnLeft = true, 5) shouldBe "0%+2"
    getLeftPos(5, showOnLeft = true, 5) shouldBe "0%+0"
    getLeftPos(5, showOnLeft = true, 10) shouldBe "0%+0"
    getLeftPos(5, showOnLeft = false, 5) shouldBe "50%+0"
    getLeftPos(10, showOnLeft = false, 5) shouldBe "50%+2"
    getLeftPos(5, showOnLeft = false, 10) shouldBe "50%-5"
    getLeftPos(5, showOnLeft = false, 11) shouldBe "0%+0"
  }

  private def assertMenuPopup(result: TestInstance, props: MenuPopupProps): Unit = {
    val textWidth = props.items.maxBy(_.length).length
    val width = textWidth + 3 * 2
    val height = 2 * 2 + props.items.size
    val theme = Theme.current.popup.menu

    assertTestComponent(result, popupComp)({
      case PopupProps(onClose, closable, focusable, _, _) =>
        onClose should be theSameInstanceAs props.onClose
        closable shouldBe true
        focusable shouldBe true
    }, inside(_) { case List(content) =>
      assertTestComponent(content, modalContentComp)({
        case ModalContentProps(title, resSize, style, padding, left) =>
          title shouldBe props.title
          resSize shouldBe width -> height
          style shouldBe theme
          padding shouldBe MenuPopup.padding
          left shouldBe props.getLeft(width)
      }, inside(_) { case lines =>
        lines.size shouldBe props.items.size
        lines.zipWithIndex.zip(props.items).foreach { case ((line, index), expected) =>
          assertTestComponent(line, buttonComp, plain = true)(inside(_) {
            case ButtonProps(left, top, label, resStyle, _) =>
              left shouldBe 1
              top shouldBe (1 + index)
              label shouldBe expected
              resStyle shouldBe theme
          })
        }
        Succeeded
      })
    })
  }
}
