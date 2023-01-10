package farjs.viewer

import farjs.filelist._
import farjs.ui.popup.PopupProps
import farjs.viewer.ViewerPluginUi._
import scommons.react.blessed._
import scommons.react.test._

class ViewerPluginUiSpec extends TestSpec with TestRendererUtils {

  ViewerPluginUi.popupComp = mockUiComponent("Popup")

  it should "call onClose when onClose" in {
    //given
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1")
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popup = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects()

    //when
    popup.onClose()
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
        <.text(
          ^.rbWidth := "100%",
          ^.rbHeight := 1,
          ^.rbStyle := headerStyle,
          ^.content := filePath
        )(),

        <.button(
          ^.rbMouse := true,
          ^.rbTop := 1,
          ^.rbWidth := "100%",
          ^.rbHeight := "100%",
          ^.rbStyle := contentStyle,
          ^.content := ""
        )()
      )
    ))
  }
}
