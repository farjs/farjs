package farjs.ui

import farjs.ui.ComboBoxPopup._
import farjs.ui.border._
import farjs.ui.theme.DefaultTheme
import scommons.react.blessed._
import scommons.react.test._

class ComboBoxPopupSpec extends TestSpec with TestRendererUtils {

  ComboBoxPopup.singleBorderComp = mockUiComponent("SingleBorder")

  it should "call onClick with item index when onClick" in {
    //given
    val onClick = mockFunction[Int, Unit]
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      onClick = onClick
    )
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val textEl = inside(findComponents(comp, <.text.name)) {
      case List(_, text) => text
    }
    
    //then
    onClick.expects(1)
    
    //when
    textEl.props.onClick(null)
  }

  it should "call onWheel(true) when text.onWheelup" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      onClick = _ => (),
      onWheel = onWheel
    )
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val textEl = inside(findComponents(comp, <.text.name)) {
      case List(_, text) => text
    }
    
    //then
    onWheel.expects(true)
    
    //when
    textEl.props.onWheelup(null)
  }

  it should "call onWheel(false) when text.onWheeldown" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      onClick = _ => (),
      onWheel = onWheel
    )
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val textEl = inside(findComponents(comp, <.text.name)) {
      case List(_, text) => text
    }
    
    //then
    onWheel.expects(false)
    
    //when
    textEl.props.onWheeldown(null)
  }

  it should "call onWheel(true) when box.onWheelup" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      onClick = _ => (),
      onWheel = onWheel
    )
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val boxEl = inside(findComponents(comp, <.box.name)) {
      case List(box) => box
    }
    
    //then
    onWheel.expects(true)
    
    //when
    boxEl.props.onWheelup(null)
  }

  it should "call onWheel(false) when box.onWheeldown" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      onClick = _ => (),
      onWheel = onWheel
    )
    val comp = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    val boxEl = inside(findComponents(comp, <.box.name)) {
      case List(box) => box
    }
    
    //then
    onWheel.expects(false)
    
    //when
    boxEl.props.onWheeldown(null)
  }

  it should "render component" in {
    //given
    val props = ComboBoxPopupProps(
      selected = 0,
      items = List("item 1", "item 2"),
      left = 1,
      top = 2,
      width = 11,
      style = DefaultTheme.popup.menu,
      _ => ()
    )
    
    //when
    val result = testRender(<(ComboBoxPopup())(^.wrapped := props)())
    
    //then
    assertComboBoxPopupPopup(result, props)
  }

  private def assertComboBoxPopupPopup(result: TestInstance, props: ComboBoxPopupProps): Unit = {
    val width = props.width
    val height = 10
    val textWidth = width - 2
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

        props.items.zipWithIndex.map { case (text, index) =>
          <.text(
            ^.rbHeight := 1,
            ^.rbWidth := textWidth,
            ^.rbLeft := 1,
            ^.rbTop := 1 + index,
            ^.rbClickable := true,
            ^.rbMouse := true,
            ^.rbAutoFocus := false,
            ^.rbStyle := {
              if (props.selected == index) theme.focus.getOrElse(null)
              else theme
            },
            ^.content := s"  ${text.take(textWidth - 4)}  "
          )()
        }
      )
    )
  }
}
