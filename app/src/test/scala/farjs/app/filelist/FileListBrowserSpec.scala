package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.app.filelist.FileListBrowserSpec._
import farjs.filelist.FileListActions.FileListActivateAction
import farjs.filelist._
import farjs.filelist.stack._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FileListBrowserSpec extends TestSpec with TestRendererUtils {
  
  FileListBrowser.panelStackComp = () => "PanelStack".asInstanceOf[ReactClass]
  FileListBrowser.fileListPanelComp = () => "FileListPanel".asInstanceOf[ReactClass]
  FileListBrowser.bottomMenuComp = () => "BottomMenu".asInstanceOf[ReactClass]

  it should "swap the panels when Ctrl+U" in {
    //given
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    
    val screenMock = mock[BlessedScreenMock]
    val buttonMock = mock[BlessedElementMock]
    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null
    (buttonMock.focus _).expects()
    (buttonMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *).onCall { (keys, listener) =>
      keys.toList shouldBe List("C-u")
      keyListener = listener
    }
    val renderer = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val List(leftPanel, rightPanel) = findProps(renderer.root, fileListPanelComp)
    leftPanel.state shouldBe props.data.left
    rightPanel.state shouldBe props.data.right
    
    //then
    (screenMock.focusNext _).expects()

    //when
    TestRenderer.act { () =>
      keyListener(null, null)
    }

    //then
    val List(newLeftPanel, newRightPanel) = findProps(renderer.root, fileListPanelComp)
    newLeftPanel.state shouldBe props.data.right
    newRightPanel.state shouldBe props.data.left
  }

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

    val screenMock = mock[BlessedScreenMock]
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
    (rightButtonMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)
    (rightButtonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock.asInstanceOf[js.Any]
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock.asInstanceOf[js.Any]
      else null
    })
    val List(leftButton, _) = findComponents(comp, <.button.name)
    
    //then
    dispatch.expects(FileListActivateAction(isRight = false))

    //when
    leftButton.props.onFocus()
  }

  it should "dispatch FileListActivateAction when onFocus in right panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())

    val screenMock = mock[BlessedScreenMock]
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
    (leftButtonMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)
    (leftButtonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock.asInstanceOf[js.Any]
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock.asInstanceOf[js.Any]
      else null
    })
    val List(_, rightButton) = findComponents(comp, <.button.name)
    
    //then
    dispatch.expects(FileListActivateAction(isRight = true))

    //when
    rightButton.props.onFocus()
  }

  it should "focus next panel when onKeypress(tab|S-tab)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())

    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
    (leftButtonMock.screen _).expects().returning(screen)
    (screenMock.key _).expects(*, *)
    (leftButtonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock.asInstanceOf[js.Any]
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock.asInstanceOf[js.Any]
      else null
    })
    val List(button, _) = findComponents(comp, <.button.name)
    
    def check(keyFull: String, focus: Boolean): Unit = {
      //then
      if (focus) {
        (leftButtonMock.screen _).expects().returning(screen)
        (screenMock.focusNext _).expects()
      }

      //when
      button.props.onKeypress(null, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])
    }
    
    //when & then
    check(keyFull = "tab", focus = true)
    check(keyFull = "S-tab", focus = true)
    check(keyFull = "unknown", focus = false)
  }

  it should "render component" in {
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

    val screenMock = mock[BlessedScreenMock]
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
    (rightButtonMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)
    
    //then
    (rightButtonMock.focus _).expects()

    //when
    val result = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock.asInstanceOf[js.Any]
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock.asInstanceOf[js.Any]
      else null
    })

    //then
    assertTestComponent(result, WithPanelStacks)({ case WithPanelStacksProps(leftStack, rightStack) =>
      leftStack should not be null
      rightStack should not be null
    }, { case List(left, right, menu) =>
      assertNativeComponent(left, <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(), { case List(stack) =>
        assertTestComponent(stack, panelStackComp)({ case PanelStackProps(isRight, _, _, _) =>
          isRight shouldBe false
        }, { case List(panel) =>
          assertTestComponent(panel, fileListPanelComp) {
            case FileListPanelProps(resDispatch, resActions, state) =>
              resDispatch should be theSameInstanceAs dispatch
              resActions should be theSameInstanceAs actions
              state shouldBe props.data.left
          }
        })
      })
      assertNativeComponent(right, <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(), { case List(stack) =>
        assertTestComponent(stack, panelStackComp)({ case PanelStackProps(isRight, _, _, _) =>
          isRight shouldBe true
        }, { case List(panel) =>
          assertTestComponent(panel, fileListPanelComp) {
            case FileListPanelProps(resDispatch, resActions, state) =>
              resDispatch should be theSameInstanceAs dispatch
              resActions should be theSameInstanceAs actions
              state shouldBe props.data.right
          }
        })
      })

      assertNativeComponent(menu,
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())()()
        )
      )
    })
  }
}

object FileListBrowserSpec {

  @JSExportAll
  trait BlessedScreenMock {

    def key(keys: js.Array[String], onKey: js.Function2[js.Object, KeyboardKey, Unit]): Unit
    def focusNext(): Unit
  }

  @JSExportAll
  trait BlessedElementMock {

    def screen: BlessedScreen
    def focus(): Unit
  }
}
