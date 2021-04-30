package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.app.filelist.FileListBrowserSpec._
import farjs.filelist.FileListActions.{FileListActivateAction, FileListDirUpdateAction}
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupExitAction
import farjs.filelist.stack._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FileListBrowserSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {
  
  FileListBrowser.panelStackComp = () => "PanelStack".asInstanceOf[ReactClass]
  FileListBrowser.fileListPanelComp = () => "FileListPanel".asInstanceOf[ReactClass]
  FileListBrowser.bottomMenuComp = () => "BottomMenu".asInstanceOf[ReactClass]

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
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
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
    
    Succeeded
  }

  it should "dispatch FileListActivateAction when onFocus in right panel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
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
    
    Succeeded
  }

  it should "dispatch FileListPopupExitAction when onKeypress(F10)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val buttonMock = mock[BlessedElementMock]
    (buttonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val List(button, _) = findComponents(comp, <.button.name)
    val keyFull = "f10"
    
    //then
    (buttonMock.screen _).expects().returning(screen)
    dispatch.expects(FileListPopupExitAction(show = true))

    //when
    button.props.onKeypress(null, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "focus next panel when onKeypress(tab|S-tab)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListBrowserProps(dispatch, actions, FileListsState())
    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val buttonMock = mock[BlessedElementMock]
    (buttonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val List(button, _) = findComponents(comp, <.button.name)
    
    def check(keyFull: String, focus: Boolean): Unit = {
      (buttonMock.screen _).expects().returning(screen)
      if (focus) {
        //then
        (screenMock.focusNext _).expects()
      }

      //when
      button.props.onKeypress(null, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])
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
    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val buttonMock = mock[BlessedElementMock]
    (buttonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val List(button, _) = findComponents(comp, <.button.name)
    val keyFull = "C-u"
    
    //then
    (buttonMock.screen _).expects().returning(screen)
    (screenMock.focusNext _).expects()

    //when
    button.props.onKeypress(screen, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    val List(leftPanel, rightPanel) = findProps(comp, fileListPanelComp)
    leftPanel.state shouldBe props.data.right
    rightPanel.state shouldBe props.data.left
  }

  it should "update active panel when onKeypress(Ctrl+R)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val leftDir = FileListDir("/left/dir", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val rightDir = FileListDir("/right/dir", isRoot = false, List(
      FileListItem("dir 2", isDir = true)
    ))
    val props = FileListBrowserProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = leftDir, isActive = true),
      right = FileListState(currDir = rightDir, isRight = true)
    ))
    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val buttonMock = mock[BlessedElementMock]
    (buttonMock.focus _).expects()

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val List(button, _) = findComponents(comp, <.button.name)
    val keyFull = "C-r"
    val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
      FileListItem("file 1")
    ))
    val action = FileListDirUpdateAction(FutureTask("Updating", Future.successful(updatedDir)))
    
    //then
    (buttonMock.screen _).expects().returning(screen)
    (actions.updateDir _).expects(dispatch, false, leftDir.path).returning(action)
    dispatch.expects(action)

    //when
    button.props.onKeypress(screen, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])

    action.task.future.map(_ => Succeeded)
  }

  it should "trigger plugin when onKeypress(triggerKey)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val onTriggerMock = mockFunction[Boolean, PanelStack, PanelStack, Unit]
    val keyFull = "C-p"
    val plugin = new FileListPlugin {
      val triggerKey: String = keyFull
      def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
        onTriggerMock(isRight, leftStack, rightStack)
      }
    }
    val props = FileListBrowserProps(dispatch, actions, FileListsState(), List(plugin))
    val screenMock = mock[BlessedScreenMock]
    val screen = screenMock.asInstanceOf[BlessedScreen]
    val buttonMock = mock[BlessedElementMock]
    (buttonMock.focus _).expects()
    (buttonMock.screen _).expects().returning(screen)

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock.asInstanceOf[js.Any]
      else null
    })
    val WithPanelStacksProps(leftStack, rightStack) = findComponentProps(comp, WithPanelStacks)
    val List(button, _) = findComponents(comp, <.button.name)
    
    //then
    onTriggerMock.expects(false, leftStack, rightStack)

    //when
    button.props.onKeypress(screen, js.Dynamic.literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "render component and focus active panel" in {
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
    val leftButtonMock = mock[BlessedElementMock]
    val rightButtonMock = mock[BlessedElementMock]
    
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
        assertTestComponent(stack, panelStackComp)({ case PanelStackProps(isRight, _, _, _, _) =>
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
        assertTestComponent(stack, panelStackComp)({ case PanelStackProps(isRight, _, _, _, _) =>
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

    def focusNext(): Unit
  }

  @JSExportAll
  trait BlessedElementMock {

    def screen: BlessedScreen
    def focus(): Unit
  }
}
