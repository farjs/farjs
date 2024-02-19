package farjs.ui.popup

import farjs.ui.popup.ListPopup._
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import farjs.ui.{ListBoxProps, WithSizeProps}
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class ListPopupSpec extends TestSpec with TestRendererUtils {

  ListPopup.popupComp = "Popup".asInstanceOf[ReactClass]
  ListPopup.modalContentComp = "ModalContent".asInstanceOf[ReactClass]
  ListPopup.withSizeComp = "WithSize".asInstanceOf[ReactClass]
  ListPopup.listBoxComp = "ListBox".asInstanceOf[ReactClass]

  it should "not call onAction if empty items when onAction" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListPopupProps(items = Nil, onAction = onAction)
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root
    val renderContent = inside(findComponents(result, withSizeComp)) {
      case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
    }
    val resultContent = createTestRenderer(renderContent).root

    //then
    onAction.expects(1).never()
    
    //when
    inside(findComponents(resultContent, listBoxComp)) {
      case List(c) => c.props.asInstanceOf[ListBoxProps].onAction(1)
    }
  }
  
  it should "call onAction when onAction" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListPopupProps(onAction = onAction)
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root
    val renderContent = inside(findComponents(result, withSizeComp)) {
      case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
    }
    val resultContent = createTestRenderer(renderContent).root

    //then
    onAction.expects(1)
    
    //when
    inside(findComponents(resultContent, listBoxComp)) {
      case List(c) => c.props.asInstanceOf[ListBoxProps].onAction(1)
    }
  }
  
  it should "render popup with empty list" in {
    //given
    val props = getListPopupProps(items = Nil)
    
    //when
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root

    //then
    assertListPopup(result, props, Nil, (60, 20), (56, 14))
  }
  
  it should "render popup with min size" in {
    //given
    val props = getListPopupProps(items = List.fill(20)("item"))
    
    //when
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root

    //then
    assertListPopup(result, props, List.fill(20)("  item "), (55, 13), (56, 14))
  }
  
  it should "render popup with max height" in {
    //given
    val props = {
      val items = List.fill(20)("item")
      getListPopupProps(items = items, selected = items.length - 1)
    }
    
    //when
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root

    //then
    assertListPopup(result, props, List.fill(20)("  item "), (60, 20), (56, 16))
  }
  
  it should "render popup with max width" in {
    //given
    val props = getListPopupProps(items = List.fill(20)(
      "iteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
    ))
    
    //when
    val result = createTestRenderer(withThemeContext(<(ListPopup())(^.plain := props)())).root

    //then
    assertListPopup(result, props, List.fill(20)(
      "  ite...eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem "
    ), (60, 20), (60, 16))
  }

  private def getListPopupProps(items: List[String] = List("item 1", "item 2"),
                                onAction: Int => Unit = _ => (),
                                selected: js.UndefOr[Int] = js.undefined): ListPopupProps = {
    ListPopupProps(
      title = "Test Title",
      items = js.Array(items: _*),
      onAction = onAction,
      onClose = () => (),
      selected = selected,
      footer = "test footer"
    )
  }

  private def assertListPopup(result: TestInstance,
                              props: ListPopupProps,
                              items: List[String],
                              screenSize: (Int, Int),
                              expectedSize: (Int, Int)): Unit = {

    val theme = DefaultTheme.popup.menu
    val (width, height) = screenSize
    val (expectedWidth, expectedHeight) = expectedSize
    val contentWidth = expectedWidth - 2 * (paddingHorizontal + 1)
    val contentHeight = expectedHeight - 2 * (paddingVertical + 1)
    
    assertComponents(result.children, List(
      <(popupComp)(^.assertPlain[PopupProps](inside(_) {
        case PopupProps(onClose, focusable, _, onKeypress) =>
          onClose.isDefined shouldBe true
          focusable shouldBe js.undefined
          onKeypress shouldBe js.undefined
      }))(
        <(withSizeComp)(^.assertPlain[WithSizeProps](inside(_) {
          case WithSizeProps(render) =>
            val content = createTestRenderer(render(width, height)).root
            
            assertNativeComponent(content, <(modalContentComp)(^.assertPlain[ModalContentProps](inside(_) {
              case ModalContentProps(title, width, height, style, padding, left, footer) =>
                title shouldBe props.title
                width shouldBe expectedWidth
                height shouldBe expectedHeight
                style shouldBe theme
                padding shouldBe ListPopup.padding
                left shouldBe js.undefined
                footer shouldBe props.footer
            }))(), inside(_) { case List(view) =>
              assertNativeComponent(view, <(listBoxComp)(^.assertPlain[ListBoxProps](inside(_) {
                case ListBoxProps(left, top, width, height, selected, resItems, style, _, onSelect) =>
                  left shouldBe 1
                  top shouldBe 1
                  width shouldBe contentWidth
                  height shouldBe contentHeight
                  selected shouldBe props.selected.getOrElse(0)
                  resItems.toList shouldBe items
                  style shouldBe theme
                  onSelect should be theSameInstanceAs props.onSelect
              }))())
            })
        }))()
      )
    ))
  }
}
