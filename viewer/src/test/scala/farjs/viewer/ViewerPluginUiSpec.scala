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
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 123)
    val props = FileListPluginUiProps(dispatch, onClose)
    val renderer = createTestRenderer(<(pluginUi())(^.plain := props)())
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "utf-8",
      size = 123,
      width = 1,
      height = 2
    )
    findComponentProps(renderer.root, viewerController).setViewport(Some(viewport))
    val popupProps = findComponentProps(renderer.root, popupComp)
    
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
    val props = FileListPluginUiProps(dispatch, onClose)
    val renderer = createTestRenderer(<(pluginUi())(^.plain := props)())
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader,
      encoding = "utf-8",
      size = 123,
      width = 1,
      height = 2
    )
    findComponentProps(renderer.root, viewerController).setViewport(Some(viewport))
    val popupProps = findComponentProps(renderer.root, popupComp)
    
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
    val props = FileListPluginUiProps(dispatch, onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findComponentProps(comp, popupComp)
    
    //then
    onClose.expects().never()

    //when
    popupProps.onKeypress("unknown") shouldBe false
  }

  it should "update props when setViewport" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi(dispatch, "item 1", 0)
    val props = FileListPluginUiProps(dispatch, onClose)
    val renderer = createTestRenderer(<(pluginUi())(^.plain := props)())
    val viewerProps = findComponentProps(renderer.root, viewerController)
    val viewport = ViewerFileViewport(
      fileReader = new MockViewerFileReader(),
      encoding = "utf-8",
      size = 110,
      width = 1,
      height = 2,
      wrap = true,
      column = 3,
      linesData = List(("test...", 55))
    )
    
    //when
    viewerProps.setViewport(Some(viewport))
    
    //then
    inside(findComponentProps(renderer.root, viewerHeader)) {
      case ViewerHeaderProps(filePath, encoding, size, column, percent) =>
        filePath shouldBe "item 1"
        encoding shouldBe viewport.encoding
        size shouldBe viewport.size
        column shouldBe viewport.column
        percent shouldBe 50
    }
    findComponentProps(renderer.root, viewerController).viewport shouldBe Some(viewport)
    findComponentProps(renderer.root, bottomMenuComp).items shouldBe {
      defaultMenuItems.updated(1, "Unwrap")
    }
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onClose = mockFunction[Unit]
    val filePath = "item 1"
    val size = 123
    val pluginUi = new ViewerPluginUi(dispatch, filePath, size)
    val props = FileListPluginUiProps(dispatch, onClose)
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
            case ViewerHeaderProps(resFilePath, resEncoding, resSize, resColumn, resPercent) =>
              resFilePath shouldBe filePath
              resEncoding shouldBe ""
              resSize shouldBe 0
              resColumn shouldBe 0
              resPercent shouldBe 0
          }))(),
  
          <.button(
            ^.rbTop := 1,
            ^.rbWidth := "100%",
            ^.rbHeight := "100%-2"
          )(
            <(viewerController())(^.assertWrapped(inside(_) {
              case ViewerControllerProps(inputRef, resDispatch, resFilePath, resSize, viewport, _, _) =>
                inputRef.current shouldBe inputMock
                resDispatch shouldBe dispatch
                resFilePath shouldBe filePath
                resSize shouldBe size
                viewport shouldBe None
            }))()
          ),
  
          <.box(^.rbTop := "100%-1")(
            <(bottomMenuComp())(^.assertWrapped(inside(_) {
              case BottomMenuProps(resMenuItems) =>
                resMenuItems shouldBe defaultMenuItems
            }))()
          )
        )
      )
    ))
  }
}
