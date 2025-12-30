package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.app.filelist.FileListRoot.FSPlugin
import farjs.filelist._
import farjs.filelist.api._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import farjs.ui.menu.BottomMenuProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.typedarray.Uint8Array

class FileListBrowserSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {
  
  FileListBrowser.withStackComp = "WithStack".asInstanceOf[ReactClass]
  FileListBrowser.bottomMenuComp = "BottomMenu".asInstanceOf[ReactClass]
  FileListBrowser.menuBarTrigger = "MenuBarTrigger".asInstanceOf[ReactClass]
  FileListBrowser.fsPlugin = new FSPlugin((s, _) => s)
  
  //noinspection TypeAnnotation
  class Actions {
    val readFile = mockFunction[String, FileListItem, Double, js.Promise[FileSource]]

    val actions = new MockFileListActions(
      new MockFileListApi(
        readFileMock = readFile
      )
    )
  }

  //noinspection TypeAnnotation
  class Source {
    val readNextBytes = mockFunction[Uint8Array, js.Promise[Int]]
    val close = mockFunction[js.Promise[Unit]]

    val source = MockFileSource(
      readNextBytesMock = readNextBytes,
      closeMock = close
    )
  }

  private val fileListBrowser: ReactClass = new FileListBrowser(FileListPluginHandler(js.Array())).apply()

  it should "not activate left stack if already active when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(comp, <.button.name)) {
      case List(leftButton, _) => leftButton
    }
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
    
    //when
    leftButton.props.onFocus()

    //then
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
  }

  it should "activate left stack when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch, isRightInitiallyActive = true)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val leftButton = inside(findComponents(comp, <.button.name)) {
      case List(leftButton, _) => leftButton
    }
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
    
    //when
    leftButton.props.onFocus()
    
    //then
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
  }

  it should "activate right stack when onFocus in right panel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch)
    val focusMock = mockFunction[Unit]
    val leftButtonMock = literal("focus" -> focusMock)
    val rightButtonMock = literal()
    focusMock.expects()

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })
    val rightButton = inside(findComponents(comp, <.button.name)) {
      case List(_, rightButton) => rightButton
    }
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe true
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe false
    }
    
    //when
    rightButton.props.onFocus()
    
    //then
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
  }

  it should "focus next panel when onKeypress(tab|S-tab)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch)
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
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
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch)
    val focusNextMock = mockFunction[Unit]
    val screen = literal("focusNext" -> focusNextMock).asInstanceOf[BlessedScreen]
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("screen" -> screen, "focus" -> focusMock)
    focusMock.expects()

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
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
    inside(findComponents(comp, withStackComp).map(_.props.asInstanceOf[WithStackProps])) {
      case List(leftStack, rightStack) =>
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
  }

  it should "call pluginHandler.openCurrItem when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val openCurrItemMock = mockFunction[Dispatch, PanelStack, Unit]
    val pluginHandler = MockFileListPluginHandler(openCurrItemMock)
    val props = FileListBrowserProps(dispatch)
    val fileListBrowser: ReactClass = new FileListBrowser(pluginHandler).apply()
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("dir 1", isDir = true)))
    )

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.isActive shouldBe true
        leftStack.update[FileListState](_.withState(currState))
    }
    val fsItem = inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.peek[FileListState]()
    }
    fsItem.state shouldBe currState
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "enter"
    val leftStack = inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack
    }
    
    //then
    openCurrItemMock.expects(props.dispatch, leftStack)

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "call pluginHandler.openCurrItem when onKeypress(C-pagedown)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val openCurrItemMock = mockFunction[Dispatch, PanelStack, Unit]
    val pluginHandler = MockFileListPluginHandler(openCurrItemMock)
    val props = FileListBrowserProps(dispatch)
    val fileListBrowser: ReactClass = new FileListBrowser(pluginHandler).apply()
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("dir 1", isDir = true)))
    )

    val comp = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.isActive shouldBe true
        leftStack.update[FileListState](_.withState(currState))
    }
    val fsItem = inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.peek[FileListState]()
    }
    fsItem.state shouldBe currState
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "C-pagedown"
    val leftStack = inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack
    }
    
    //then
    openCurrItemMock.expects(props.dispatch, leftStack)

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    Succeeded
  }

  it should "call pluginHandler.openPluginUi and render plugin ui when onKeypress(triggerKey)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val keyFull = "C-p"
    val openPluginUiMock = mockFunction[Dispatch, KeyboardKey, WithStacksProps, js.Promise[js.UndefOr[ReactClass]]]
    val pluginHandler = MockFileListPluginHandler(openPluginUiMock = openPluginUiMock)
    val props = FileListBrowserProps(dispatch)
    val fileListBrowser: ReactClass = new FileListBrowser(pluginHandler).apply()
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val renderer = createTestRenderer(<(fileListBrowser)(^.plain := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val stacks = findComponents(renderer.root, WithStacks).map(_.props.asInstanceOf[WithStacksProps]).head
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button, _) => button
    }
    val pluginUi = "test_plugin_ui".asInstanceOf[ReactClass]
    val keyData = literal(full = keyFull).asInstanceOf[KeyboardKey]
    
    //then
    openPluginUiMock.expects(props.dispatch, keyData, *).onCall { (_, _, resStacks) =>
      WithStacksSpec.assertStacks(resStacks, stacks)
      js.Promise.resolve[js.UndefOr[ReactClass]](pluginUi)
    }

    //when
    button.props.onKeypress(null, keyData)

    //then
    eventually(findComponents(renderer.root, pluginUi) should not be empty).map { _ =>
      inside(findComponents(renderer.root, pluginUi)) {
        case List(uiComp) =>
          var resOnClose: js.Function0[Unit] = null
          assertNativeComponent(uiComp, <(pluginUi)(^.assertPlain[FileListPluginUiProps](inside(_) {
            case FileListPluginUiProps(resDispatch, onClose) =>
              resDispatch shouldBe dispatch
              resOnClose = onClose
          }))())

          //when
          resOnClose()

          //then
          findComponents(renderer.root, pluginUi) should be(empty)
      }
    }
  }

  it should "render initial component and focus active panel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val props = FileListBrowserProps(dispatch, isRightInitiallyActive = true)
    val leftButtonMock = literal()
    val focusMock = mockFunction[Unit]
    val rightButtonMock = literal("focus" -> focusMock)
    
    //then
    focusMock.expects()

    //when
    val result = testRender(<(fileListBrowser)(^.plain := props)(), { el =>
      val isRight = el.props.isRight.asInstanceOf[js.UndefOr[Boolean]].getOrElse(false)
      if (isRight && el.`type` == <.button.name.asInstanceOf[js.Any]) rightButtonMock
      else if (el.`type` == <.button.name.asInstanceOf[js.Any]) leftButtonMock
      else null
    })

    //then
    assertFileListBrowser(result)
  }
  
  private def assertFileListBrowser(result: TestInstance): Assertion = {
    assertNativeComponent(result, <(WithStacks)(^.assertPlain[WithStacksProps](inside(_) {
      case WithStacksProps(WithStacksData(leftStack, _), WithStacksData(rightStack, _)) =>
        leftStack should not be null
        rightStack should not be null
    }))(
      <.button(
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(
        <(withStackComp)(^.assertPlain[WithStackProps](inside(_) {
          case WithStackProps(isRight, _, stack, _, _) =>
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
        <(withStackComp)(^.assertPlain[WithStackProps](inside(_) {
          case WithStackProps(isRight, _, stack, _, _) =>
            isRight shouldBe true
            stack.isActive shouldBe true
        }))()
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp)(^.plain := BottomMenuProps(menuItems))()
      ),
      <(menuBarTrigger)()()
    ))
  }
}

object MockFileListPluginHandler {

  //noinspection NotImplementedCode
  def apply(
             openCurrItemMock: (Dispatch, PanelStack) => Unit = (_, _) => ???,
             openPluginUiMock: (Dispatch, KeyboardKey, WithStacksProps) => js.Promise[js.UndefOr[ReactClass]] = (_, _, _) => ???
           ): FileListPluginHandler = {

    new FileListPluginHandler {

      def openCurrItem(dispatch: Dispatch, stack: PanelStack): Unit =
        openCurrItemMock(dispatch, stack)

      def openPluginUi(dispatch: Dispatch, key: KeyboardKey, stacks: WithStacksProps): js.Promise[js.UndefOr[ReactClass]] =
        openPluginUiMock(dispatch, key, stacks)
    }
  }
}
