package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.filelist._
import farjs.filelist.fs.{FSDrivePopupProps, FSPlugin}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupExitAction
import farjs.filelist.stack._
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListBrowserSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {
  
  FileListBrowser.panelStackComp = mockUiComponent("PanelStack")
  FileListBrowser.fsDrivePopup = mockUiComponent("FSDrivePopup")
  FileListBrowser.bottomMenuComp = mockUiComponent("BottomMenu")
  FileListBrowser.fsPlugin = new FSPlugin((_, s, _) => s)
  FileListBrowser.fileListPopups = "test_popups".asInstanceOf[ReactClass]
  
  it should "not activate left stack if already active when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(comp, <.button.name)) {
      case List(leftButton, _) => leftButton
    }
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
    
    //when
    leftButton.props.onFocus()

    //then
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
  }

  it should "activate left stack when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch, isRightInitiallyActive = true)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(comp, <.button.name)) {
      case List(leftButton, _) => leftButton
    }
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
    
    //when
    leftButton.props.onFocus()
    
    //then
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
  }

  it should "activate right stack when onFocus in right panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val rightButton = inside(findComponents(comp, <.button.name)) {
      case List(_, rightButton) => rightButton
    }
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
    
    //when
    rightButton.props.onFocus()
    
    //then
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
  }

  it should "dispatch FileListPopupExitAction when onKeypress(F10)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "f10"
    
    //then
    dispatch.expects(FileListPopupExitAction(show = true))

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "focus next panel when onKeypress(tab|S-tab)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch)
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    
    def check(keyFull: String, focus: Boolean): Unit = {
      if (focus) {
        //then
        focusNextMock.expects()
      }

      //when
      button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    }
    
    //when & then
    check(keyFull = "tab", focus = true)
    check(keyFull = "S-tab", focus = true)
    check(keyFull = "unknown", focus = false)

    Succeeded
  }

  it should "swap the panels when onKeypress(Ctrl+U)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch)
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
    val keyFull = "C-u"
    
    //then
    focusNextMock.expects()

    //when
    button.props.onKeypress(screen, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe true
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe false
        rightStack.stack.isActive shouldBe true
    }
  }

  it should "trigger plugin when onKeypress(triggerKey)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTriggerMock = mockFunction[Boolean, PanelStack, PanelStack, Unit]
    val keyFull = "C-p"
    val plugin = new FileListPlugin {
      val triggerKey: String = keyFull
      def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
        onTriggerMock(isRight, leftStack, rightStack)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val WithPanelStacksProps(leftStack, rightStack) = findComponentProps(comp, WithPanelStacks)
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    
    //then
    onTriggerMock.expects(false, leftStack, rightStack)

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "show Drive popup on the left when onKeypress(Alt+L)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val keyFull = "M-l"
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val renderer = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(renderer.root, <.button.name)) {
      case List(leftButton, _) => leftButton
    }

    //when
    TestRenderer.act { () =>
      leftButton.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    }

    //then
    inside(findComponentProps(renderer.root, fsDrivePopup)) {
      case FSDrivePopupProps(resDispatch, onClose, showOnLeft) =>
        resDispatch shouldBe dispatch
        showOnLeft shouldBe true
        
        //when
        onClose()
        
        //then
        findProps(renderer.root, fsDrivePopup) should be (empty)
    }
  }
  
  it should "show Drive popup on the right when onKeypress(Alt+R)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val keyFull = "M-r"
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val renderer = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(renderer.root, <.button.name)) {
      case List(leftButton, _) => leftButton
    }

    //when
    TestRenderer.act { () =>
      leftButton.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    }

    //then
    inside(findComponentProps(renderer.root, fsDrivePopup)) {
      case FSDrivePopupProps(resDispatch, onClose, showOnLeft) =>
        resDispatch shouldBe dispatch
        showOnLeft shouldBe false
        
        //when
        onClose()
        
        //then
        findProps(renderer.root, fsDrivePopup) should be (empty)
    }
  }
  
  it should "render initial component and focus active panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListBrowserProps(dispatch, isRightInitiallyActive = true)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    
    //then
    focusMock.expects()

    //when
    val result = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })

    //then
    assertFileListBrowser(result)
  }
  
  private def assertFileListBrowser(result: TestInstance): Assertion = {
    assertNativeComponent(result, <(WithPanelStacks())(^.assertWrapped(inside(_) {
      case WithPanelStacksProps(leftStack, rightStack) =>
        leftStack should not be null
        rightStack should not be null
    }))(
      <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(
        <(panelStackComp())(^.assertWrapped(inside(_) {
          case PanelStackProps(isRight, _, stack, _, _) =>
            isRight shouldBe false
            stack.isActive shouldBe false
        }))()
      ),
      <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(
        <(panelStackComp())(^.assertWrapped(inside(_) {
          case PanelStackProps(isRight, _, stack, _, _) =>
            isRight shouldBe true
            stack.isActive shouldBe true
        }))()
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp())()()
      ),
      <(fileListPopups).empty
    ))
  }
}
