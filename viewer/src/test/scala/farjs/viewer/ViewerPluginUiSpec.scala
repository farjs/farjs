package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.BottomMenuProps
import farjs.ui.popup.PopupProps
import farjs.viewer.ViewerPluginUi._
import scommons.react.blessed._
import scommons.react.test._

class ViewerPluginUiSpec extends TestSpec with TestRendererUtils {

  ViewerPluginUi.popupComp = mockUiComponent("Popup")
  ViewerPluginUi.bottomMenuComp = mockUiComponent("BottomMenu")

  it should "call onClose when onClose" in {
    //given
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1")
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects()

    //when
    popupProps.onClose()
  }

  it should "call onClose when onKeypress(F10)" in {
    //given
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1")
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects()

    //when
    popupProps.onKeypress("f10") shouldBe true
  }

  it should "do nothing when onKeypress(unknown)" in {
    //given
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1")
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects().never()

    //when
    popupProps.onKeypress("unknown") shouldBe false
  }

  it should "render component" in {
    //given
    val onClose = mockFunction[Unit]
    val filePath = "item 1"
    val pluginUi = new ViewerPluginUi(filePath)
    val props = FileListPluginUiProps(onClose = onClose)
    
    //when
    val result = createTestRenderer(<(pluginUi())(^.plain := props)()).root

    //then
    assertComponents(result.children, List(
      <(popupComp())(^.assertWrapped(inside(_) {
        case PopupProps(_, closable, focusable, _, _) =>
          closable shouldBe true
          focusable shouldBe true
      }))(
        <.box(
          ^.rbClickable := true,
          ^.rbAutoFocus := false
        )(
          <.text(
            ^.rbWidth := "100%",
            ^.rbHeight := 1,
            ^.rbStyle := headerStyle,
            ^.content := filePath
          )(),
  
          <.button(
            ^.rbTop := 1,
            ^.rbWidth := "100%",
            ^.rbHeight := "100%-2",
            ^.rbStyle := contentStyle,
            ^.content := ""
          )(),
  
          <.box(^.rbTop := "100%-1")(
            <(bottomMenuComp())(^.wrapped := BottomMenuProps(menuItems))()
          )
        )
      )
    ))
  }
}
