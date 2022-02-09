package farjs.app.filelist

import farjs.filelist.FileListActions.FileListActivateAction
import farjs.filelist._
import farjs.filelist.api.FileListApi
import farjs.filelist.fs.FSDrivePopupProps
import farjs.filelist.popups.FileListPopupsActions.FileListPopupExitAction
import farjs.filelist.stack._
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.redux.Dispatch
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FileListBrowserSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {
  
  private val fileListBrowser = new FileListBrowser("test_fsController".asInstanceOf[ReactClass])

  fileListBrowser.panelStackComp = mockUiComponent("PanelStack")
  fileListBrowser.fsDrivePopup = mockUiComponent("FSDrivePopup")
  fileListBrowser.bottomMenuComp = mockUiComponent("BottomMenu")
  
  import fileListBrowser._

  it should "dispatch FileListActivateAction when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val data = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = false),
        right = state.right.copy(isActive = true)
      )
    }
    val props = FileListBrowserProps(dispatch, actions, data)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(comp, <.button.name)) {
      case List(leftButton, _) => leftButton
    }
    
    //then
    dispatch.expects(FileListActivateAction(isRight = false))

    //when
    leftButton.props.onFocus()
    
    Succeeded
  }

  it should "dispatch FileListActivateAction when onFocus in right panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val rightButton = inside(findComponents(comp, <.button.name)) {
      case List(_, rightButton) => rightButton
    }
    
    //then
    dispatch.expects(FileListActivateAction(isRight = true))

    //when
    rightButton.props.onFocus()
    
    Succeeded
  }

  it should "dispatch FileListPopupExitAction when onKeypress(F10)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
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
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
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
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    inside(findProps(comp, panelStackComp)) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        rightStack.isRight shouldBe true
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
        rightStack.isRight shouldBe false
    }
  }

  it should "trigger plugin when onKeypress(triggerKey)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new FileListActions {
      protected def api: FileListApi = ???
    }
    val onTriggerMock = mockFunction[Boolean, PanelStack, PanelStack, Unit]
    val keyFull = "C-p"
    val plugin = new FileListPlugin {
      val triggerKey: String = keyFull
      def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
        onTriggerMock(isRight, leftStack, rightStack)
      }
    }
    val props = FileListBrowserProps(dispatch, actions, FileListsState(), List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
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
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val data = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = true),
        right = state.right.copy(isActive = false)
      )
    }
    val keyFull = "M-l"
    val props = FileListBrowserProps(dispatch, actions, data)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val renderer = createTestRenderer(<(fileListBrowser())(^.wrapped := props)(), { el =>
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
      case FSDrivePopupProps(resDispatch, resActions, isRight, onClose, showOnLeft) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        isRight shouldBe false
        showOnLeft shouldBe true
        
        //when
        onClose()
        
        //then
        findProps(renderer.root, fsDrivePopup) should be (empty)
    }
  }
  
  it should "show Drive popup on the right when onKeypress(Alt+R)" in {
    //given
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val data = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = true),
        right = state.right.copy(isActive = false)
      )
    }
    val keyFull = "M-r"
    val props = FileListBrowserProps(dispatch, actions, data)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val renderer = createTestRenderer(<(fileListBrowser())(^.wrapped := props)(), { el =>
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
      case FSDrivePopupProps(resDispatch, resActions, isRight, onClose, showOnLeft) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        isRight shouldBe true
        showOnLeft shouldBe false
        
        //when
        onClose()
        
        //then
        findProps(renderer.root, fsDrivePopup) should be (empty)
    }
  }
  
  it should "render initial component and focus active panel" in {
    //given
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val data = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = false),
        right = state.right.copy(isActive = true)
      )
    }
    val props = FileListBrowserProps(dispatch, actions, data)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    
    //then
    focusMock.expects()

    //when
    val result = testRender(<(fileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })

    //then
    assertFileListBrowser(result, props)
  }
  
  private def assertFileListBrowser(result: TestInstance, props: FileListBrowserProps): Assertion = {
    assertTestComponent(result, WithPanelStacks)({ case WithPanelStacksProps(leftStack, rightStack) =>
      leftStack should not be null
      rightStack should not be null
    }, inside(_) { case List(left, right, menu) =>
      assertNativeComponent(left, <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(), inside(_) { case List(stack) =>
        assertTestComponent(stack, panelStackComp) { case PanelStackProps(isRight, _, _, _, _) =>
          isRight shouldBe false
        }
      })
      assertNativeComponent(right, <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(), inside(_) { case List(stack) =>
        assertTestComponent(stack, panelStackComp) { case PanelStackProps(isRight, _, _, _, _) =>
          isRight shouldBe true
        }
      })

      assertNativeComponent(menu,
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())()()
        )
      )
    })
  }
}
