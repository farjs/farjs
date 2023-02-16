package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.BottomMenuProps
import farjs.ui.popup.PopupProps
import farjs.viewer.ViewerPluginUi._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ViewerPluginUiSpec extends TestSpec with TestRendererUtils {

  ViewerPluginUi.popupComp = mockUiComponent("Popup")
  ViewerPluginUi.viewerHeader = mockUiComponent("ViewerHeader")
  ViewerPluginUi.viewerController = mockUiComponent("ViewerController")
  ViewerPluginUi.bottomMenuComp = mockUiComponent("BottomMenu")

  it should "call onClose when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 0)
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
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 0)
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
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 0)
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects().never()

    //when
    popupProps.onKeypress("unknown") shouldBe false
  }

  it should "update progress when onViewProgress" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 0)
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val viewerProps = findComponentProps(comp, viewerController)
    val percent = 50
    
    //when
    viewerProps.onViewProgress(percent)
    
    //then
    findComponentProps(comp, viewerHeader).percent shouldBe percent
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val filePath = "item 1"
    val size = 123
    val pluginUi = new ViewerPluginUi(dispatch, filePath, size)
    val props = FileListPluginUiProps(onClose = onClose)
    val inputMock = js.Dynamic.literal()
    
    //when
    val result = createTestRenderer(<(pluginUi())(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) inputMock
      else null
    }).root

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
          <(viewerHeader())(^.assertWrapped(inside(_) {
            case ViewerHeaderProps(resFilePath, resEncoding, resSize, resPercent) =>
              resFilePath shouldBe filePath
              resEncoding shouldBe "utf-8"
              resSize shouldBe size
              resPercent shouldBe 0
          }))(),
  
          <.button(
            ^.rbTop := 1,
            ^.rbWidth := "100%",
            ^.rbHeight := "100%-2"
          )(
            <(viewerController())(^.assertWrapped(inside(_) {
              case ViewerControllerProps(inputRef, resDispatch, resFilePath, resEncoding, resSize, _) =>
                inputRef.current shouldBe inputMock
                resDispatch shouldBe dispatch
                resFilePath shouldBe filePath
                resEncoding shouldBe "utf-8"
                resSize shouldBe size
            }))()
          ),
  
          <.box(^.rbTop := "100%-1")(
            <(bottomMenuComp())(^.assertWrapped(inside(_) {
              case BottomMenuProps(resMenuItems) =>
                resMenuItems shouldBe menuItems
            }))()
          )
        )
      )
    ))
  }
}
