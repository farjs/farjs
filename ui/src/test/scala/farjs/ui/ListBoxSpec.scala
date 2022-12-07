package farjs.ui

import farjs.ui.ListBox._
import farjs.ui.theme.DefaultTheme
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js.Dynamic.literal

class ListBoxSpec extends TestSpec with TestRendererUtils {

  ListBox.listViewComp = mockUiComponent("ListView")
  ListBox.scrollBarComp = mockUiComponent("ScrollBar")

  it should "scroll when onChange in ScrollBar" in {
    //given
    val props = getListBoxProps(height = 1)
    val renderer = createTestRenderer(<(ListBox())(^.wrapped := props)())
    assertListBox(renderer.root, props, showScrollBar = true)
    val scrollBarProps = findComponentProps(renderer.root, scrollBarComp, plain = true)
    val offset = 1
    
    //when
    scrollBarProps.onChange(offset)
    
    //then
    assertListBox(renderer.root, props, showScrollBar = true, offset)
  }

  it should "update viewport state when onKeypress(down)" in {
    //given
    val props = getListBoxProps()
    val renderer = createTestRenderer(<(ListBox())(^.wrapped := props)())
    assertListBox(renderer.root, props, showScrollBar = false)
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button) => button
    }
    
    //when
    button.props.onKeypress(null, literal(full = "down"))
    
    //then
    assertListBox(renderer.root, props.copy(selected = 1), showScrollBar = false)
  }

  it should "call onAction when onKeypress(return)" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListBoxProps(selected = 1, onAction = onAction)
    val renderer = createTestRenderer(<(ListBox())(^.wrapped := props)())
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button) => button
    }

    //then
    onAction.expects(1)

    //when
    button.props.onKeypress(null, literal(full = "return"))
  }

  it should "render without ScrollBar" in {
    //given
    val props = getListBoxProps(width = 10, items = List.fill(5)("item"))
    
    //when
    val result = createTestRenderer(<(ListBox())(^.wrapped := props)()).root

    //then
    assertListBox(result, props, showScrollBar = false)
  }

  it should "render with ScrollBar" in {
    //given
    val props = getListBoxProps(width = 5, height = 20, items = List.fill(25)("item"))
    
    //when
    val result = createTestRenderer(<(ListBox())(^.wrapped := props)()).root

    //then
    assertListBox(result, props, showScrollBar = true)
  }

  private def getListBoxProps(width: Int = 20,
                              height: Int = 30,
                              selected: Int = 0,
                              items: List[String] = List(
                                "  item 1",
                                "  item 2"
                              ),
                              onAction: Int => Unit = _ => ()): ListBoxProps = {
    ListBoxProps(
      left = 2,
      top = 2,
      width = width,
      height = height,
      selected = selected,
      items = items,
      style = DefaultTheme.popup.menu,
      onAction = onAction
    )
  }

  private def assertListBox(result: TestInstance,
                            props: ListBoxProps,
                            showScrollBar: Boolean,
                            offset: Int = 0): Unit = {

    assertComponents(result.children, List(
      <.button(
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbWidth := props.width,
        ^.rbHeight := props.height
      )(
        <(listViewComp())(^.assertWrapped(inside(_) {
          case ListViewProps(left, top, resWidth, resHeight, items, viewport, _, style, onClick) =>
            left shouldBe 0
            top shouldBe 0
            resWidth shouldBe props.width
            resHeight shouldBe props.height
            items shouldBe props.items
            viewport.offset shouldBe offset
            viewport.focused shouldBe props.selected
            style shouldBe props.style
            onClick should be theSameInstanceAs props.onAction
        }))(),

        if (showScrollBar) Some {
          <(scrollBarComp())(^.assertPlain[ScrollBarProps](inside(_) {
            case ScrollBarProps(left, top, length, style, value, extent, min, max, _) =>
              left shouldBe props.width
              top shouldBe 0
              length shouldBe props.height
              style shouldBe props.style
              value shouldBe offset
              extent shouldBe props.height
              min shouldBe 0
              max shouldBe (props.items.size - props.height)
          }))()
        }
        else None
      )
    ))
  }
}
