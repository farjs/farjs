package farjs.ui

import farjs.ui.ComboBoxPopup._
import farjs.ui.border._
import farjs.ui.theme.DefaultTheme
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ComboBoxPopupSpec extends TestSpec with TestRendererUtils {

  ComboBoxPopup.singleBorderComp = mockUiComponent("SingleBorder")
  ComboBoxPopup.listViewComp = mockUiComponent("ListView")
  ComboBoxPopup.scrollBarComp = mockUiComponent("ScrollBar")
  
  it should "call setViewport when box.onWheelup" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getComboBoxPopupProps(index = 1, setViewport = setViewport)
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val boxEl = inside(findComponents(comp, <.box.name)) {
      case List(box) => box
    }
    val expectedViewport = props.viewport.copy(focused = 0)
    
    //then
    setViewport.expects(expectedViewport)
    
    //when
    boxEl.props.onWheelup(null)
  }

  it should "call setViewport when box.onWheeldown" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getComboBoxPopupProps(setViewport = setViewport)
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val boxEl = inside(findComponents(comp, <.box.name)) {
      case List(box) => box
    }
    val expectedViewport = props.viewport.copy(focused = 1)
    
    //then
    setViewport.expects(expectedViewport)
    
    //when
    boxEl.props.onWheeldown(null)
  }

  it should "call setViewport when onChange in ScrollBar" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getComboBoxPopupProps(items = List.fill(15)("item"), setViewport = setViewport)
    props.items.length should be > maxItems
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val scrollBarProps = findComponentProps(comp, scrollBarComp, plain = true)
    val offset = 1
    val expectedViewport = props.viewport.copy(offset = offset)
    
    //then
    setViewport.expects(expectedViewport)
    
    //when
    scrollBarProps.onChange(offset)
  }

  it should "render without ScrollBar" in {
    //given
    val props = getComboBoxPopupProps()
    
    //when
    val result = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    
    //then
    assertComboBoxPopupPopup(result, props, showScrollBar = false)
  }

  it should "render with ScrollBar" in {
    //given
    val props = getComboBoxPopupProps(items = List.fill(15)("item"))
    
    //when
    val result = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    
    //then
    assertComboBoxPopupPopup(result, props, showScrollBar = true)
  }
  
  private def getComboBoxPopupProps(index: Int = 0,
                                    items: List[String] = List("item 1", "item 2"),
                                    setViewport: js.Function1[ListViewport, Unit] = _ => (),
                                    onClick: Int => Unit = _ => ()): ComboBoxPopupProps = {
    ComboBoxPopupProps(
      items = items,
      left = 1,
      top = 2,
      width = 11,
      viewport = ListViewport(index, items.size, maxItems),
      setViewport = setViewport,
      style = DefaultTheme.popup.menu,
      onClick = onClick
    )
  }

  private def assertComboBoxPopupPopup(result: TestInstance,
                                       props: ComboBoxPopupProps,
                                       showScrollBar: Boolean): Unit = {
    val width = props.width
    val height = maxItems + 2
    val viewWidth = width - 2
    val theme = props.style

    assertNativeComponent(result,
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbStyle := theme
      )(
        <(singleBorderComp())(^.assertPlain[SingleBorderProps](inside(_) {
          case SingleBorderProps(resWidth, resHeight, style) =>
            resWidth shouldBe width
            resHeight shouldBe height
            style shouldBe theme
        }))(),

        <(listViewComp())(^.assertWrapped(inside(_) {
          case ListViewProps(left, top, resWidth, resHeight, items, viewport, setViewport, style, onClick) =>
            left shouldBe 1
            top shouldBe 1
            resWidth shouldBe viewWidth
            resHeight shouldBe (height - 2)
            items shouldBe props.items.map(i => s"  ${i.take(viewWidth - 4)}  ")
            viewport should be theSameInstanceAs props.viewport
            setViewport should be theSameInstanceAs props.setViewport
            style shouldBe theme
            onClick should be theSameInstanceAs props.onClick
        }))(),

        if (showScrollBar) Some {
          <(scrollBarComp())(^.assertPlain[ScrollBarProps](inside(_) {
            case ScrollBarProps(left, top, length, style, value, extent, min, max, _) =>
              left shouldBe (width - 1)
              top shouldBe 1
              length shouldBe maxItems
              style shouldBe theme
              value shouldBe 0
              extent shouldBe maxItems
              min shouldBe 0
              max shouldBe (props.items.size - maxItems)
          }))()
        }
        else None
      )
    )
  }
}
