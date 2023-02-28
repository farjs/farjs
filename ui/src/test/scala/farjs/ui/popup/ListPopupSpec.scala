package farjs.ui.popup

import farjs.ui.popup.ListPopup._
import farjs.ui.theme.DefaultTheme
import farjs.ui.{ListBoxProps, WithSizeProps}
import scommons.react.test._

class ListPopupSpec extends TestSpec with TestRendererUtils {

  ListPopup.popupComp = mockUiComponent("Popup")
  ListPopup.modalContentComp = mockUiComponent("ModalContent")
  ListPopup.withSizeComp = mockUiComponent("WithSize")
  ListPopup.listBoxComp = mockUiComponent("ListBox")

  it should "not call onAction if empty items when onAction" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListPopupProps(items = Nil, onAction = onAction)
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root
    val renderContent = findComponentProps(result, withSizeComp, plain = true).render(60, 20)
    val resultContent = createTestRenderer(renderContent).root

    //then
    onAction.expects(1).never()
    
    //when
    findComponentProps(resultContent, listBoxComp, plain = true).onAction(1)
  }
  
  it should "call onAction when onAction" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListPopupProps(onAction = onAction)
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root
    val renderContent = findComponentProps(result, withSizeComp, plain = true).render(60, 20)
    val resultContent = createTestRenderer(renderContent).root

    //then
    onAction.expects(1)
    
    //when
    findComponentProps(resultContent, listBoxComp, plain = true).onAction(1)
  }
  
  it should "render popup with empty list" in {
    //given
    val props = getListPopupProps(items = Nil)
    
    //when
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root

    //then
    assertListPopup(result, props, Nil, (60, 20), (56, 14))
  }
  
  it should "render popup with min size" in {
    //given
    val props = getListPopupProps(items = List.fill(20)("item"))
    
    //when
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root

    //then
    assertListPopup(result, props, List.fill(20)("  item "), (55, 13), (56, 14))
  }
  
  it should "render popup with max height" in {
    //given
    val props = {
      val props = getListPopupProps(items = List.fill(20)("item"))
      props.copy(selected = props.items.length - 1)
    }
    
    //when
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root

    //then
    assertListPopup(result, props, List.fill(20)("  item "), (60, 20), (56, 16))
  }
  
  it should "render popup with max width" in {
    //given
    val props = getListPopupProps(items = List.fill(20)(
      "iteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
    ))
    
    //when
    val result = createTestRenderer(<(ListPopup())(^.wrapped := props)()).root

    //then
    assertListPopup(result, props, List.fill(20)(
      "  ite...eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem "
    ), (60, 20), (60, 16))
  }

  private def getListPopupProps(items: List[String] = List("item 1", "item 2"),
                                onAction: Int => Unit = _ => ()): ListPopupProps = {
    ListPopupProps(
      title = "Test Title",
      items = items,
      onAction = onAction,
      onClose = () => (),
      footer = Some("test footer")
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
      <(popupComp())(^.assertWrapped(inside(_) {
        case PopupProps(onClose, closable, focusable, _, onKeypress) =>
          onClose should be theSameInstanceAs props.onClose
          closable shouldBe true
          focusable shouldBe true
          onKeypress should be theSameInstanceAs props.onKeypress
      }))(
        <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
          case WithSizeProps(render) =>
            val content = createTestRenderer(render(width, height)).root
            
            assertTestComponent(content, modalContentComp)({
              case ModalContentProps(title, size, style, padding, left, footer) =>
                title shouldBe props.title
                size shouldBe (expectedWidth -> expectedHeight)
                style shouldBe theme
                padding shouldBe ListPopup.padding
                left shouldBe "center"
                footer shouldBe props.footer
            }, inside(_) { case List(view) =>
              assertTestComponent(view, listBoxComp, plain = true) {
                case ListBoxProps(left, top, width, height, selected, resItems, style, _, onSelect) =>
                  left shouldBe 1
                  top shouldBe 1
                  width shouldBe contentWidth
                  height shouldBe contentHeight
                  selected shouldBe props.selected
                  resItems.toList shouldBe items
                  style shouldBe theme
                  onSelect should be theSameInstanceAs props.onSelect
              }
            })
        }))()
      )
    ))
  }
}
