package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.BottomMenuProps
import farjs.ui.popup.PopupProps
import farjs.viewer.ViewerPluginUi._
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ViewerPluginUiSpec extends TestSpec with TestRendererUtils {

  ViewerPluginUi.popupComp = "Popup".asInstanceOf[ReactClass]
  ViewerPluginUi.viewerHeader = mockUiComponent("ViewerHeader")
  ViewerPluginUi.viewerController = mockUiComponent("ViewerController")
  ViewerPluginUi.bottomMenuComp = "BottomMenu".asInstanceOf[ReactClass]

  it should "call onClose when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1", 123)
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
    val popupProps = findPopupProps(renderer.root)
    
    //then
    onClose.expects()

    //when
    popupProps.onClose.foreach(_.apply())
  }

  it should "call onClose when onKeypress(F10)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1", 0)
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
    val popupProps = findPopupProps(renderer.root)
    
    //then
    onClose.expects()

    //when
    popupProps.onKeypress.map(_.apply("f10")).getOrElse(false) shouldBe true
  }

  it should "do nothing when onKeypress(unknown)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1", 0)
    val props = FileListPluginUiProps(dispatch, onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val popupProps = findPopupProps(comp)
    
    //then
    onClose.expects().never()

    //when
    popupProps.onKeypress.map(_.apply("unknown")).getOrElse(false) shouldBe false
  }

  it should "update props when setViewport" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val pluginUi = new ViewerPluginUi("item 1", 0)
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
    val bottomMenuProps = inside(findComponents(renderer.root, bottomMenuComp)) {
      case List(comp) => comp.props.asInstanceOf[BottomMenuProps]
    }
    bottomMenuProps.items.toList shouldBe {
      defaultMenuItems.updated(1, "Unwrap").toList
    }
  }

  it should "render initial component" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val onClose = mockFunction[Unit]
    val filePath = "item 1"
    val size = 123
    val pluginUi = new ViewerPluginUi(filePath, size)
    val props = FileListPluginUiProps(dispatch, onClose)
    val inputMock = js.Dynamic.literal()
    
    //when
    val result = createTestRenderer(<(pluginUi())(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) inputMock
      else null
    }).root

    //then
    assertComponents(result.children, List(
      <(popupComp)(^.assertPlain[PopupProps](inside(_) {
        case PopupProps(onClose, focusable, _, _) =>
          onClose.isDefined shouldBe true
          focusable shouldBe js.undefined
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
            <(bottomMenuComp)(^.assertPlain[BottomMenuProps](inside(_) {
              case BottomMenuProps(resMenuItems) =>
                resMenuItems shouldBe defaultMenuItems
            }))()
          )
        )
      )
    ))
  }

  private def findPopupProps(root: TestInstance): PopupProps = {
    inside(findComponents(root, popupComp)) {
      case List(popup) => popup.props.asInstanceOf[PopupProps]
    }
  }
}
