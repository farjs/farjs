package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api._
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.stack._
import farjs.fs.FSPlugin
import farjs.fs.popups.FSPopupsActions._
import farjs.ui.menu.BottomMenuProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.typedarray.Uint8Array

class FileListBrowserSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {
  
  FileListBrowser.panelStackComp = mockUiComponent("PanelStack")
  FileListBrowser.bottomMenuComp = mockUiComponent("BottomMenu")
  FileListBrowser.fsPlugin = new FSPlugin((s, _) => s)
  FileListBrowser.fileListPopups = "test_filelist_popups".asInstanceOf[ReactClass]
  FileListBrowser.fsPopups = "test_fs_popups".asInstanceOf[ReactClass]
  
  //noinspection TypeAnnotation
  class Actions {
    val readFile = mockFunction[List[String], FileListItem, Double, Future[FileSource]]

    val actions = new MockFileListActions(
      readFileMock = readFile
    )
  }

  //noinspection TypeAnnotation
  class Source {
    val readNextBytes = mockFunction[Uint8Array, Future[Int]]
    val close = mockFunction[Future[Unit]]

    val source = new MockFileSource(
      readNextBytesMock = readNextBytes,
      closeMock = close
    )
  }

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

  it should "dispatch actions when onKeypress(Alt+L/Alt+R/Alt+H/Ctrl+D/F9/F10)" in {
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

    def check(fullKey: String, action: Any): Unit = {
      //then
      dispatch.expects(action)
  
      //when
      button.props.onKeypress(null, literal(full = fullKey).asInstanceOf[KeyboardKey])
    }

    //when & then
    check("M-l", DrivePopupAction(show = ShowDriveOnLeft))
    check("M-r", DrivePopupAction(show = ShowDriveOnRight))
    check("M-h", FoldersHistoryPopupAction(show = true))
    check("C-d", FolderShortcutsPopupAction(show = true))
    check("f9", FileListPopupMenuAction(show = true))
    check("f10", FileListPopupExitAction(show = true))

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
        leftStack.isRight shouldBe false
        leftStack.stack.isActive shouldBe false
        rightStack.isRight shouldBe true
        rightStack.stack.isActive shouldBe true
    }
  }

  it should "not trigger plugin if dir when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTriggerMock = mockFunction[String, Uint8Array, () => Unit, Option[PanelStackItem[FileListState]]]
    val plugin = new FileListPlugin {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("dir 1", isDir = true)))
    )

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.isActive shouldBe true
      leftStack.update[FileListState](_.withState(currState))
    }
    val fsItem = inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState]
    }
    fsItem.state shouldBe Some(currState)
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "enter"
    
    //then
    onTriggerMock.expects(*, *, *).never()

    //when & then
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState] shouldBe fsItem
    }

    Succeeded
  }

  it should "not trigger plugin if not local FS when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTriggerMock = mockFunction[String, Uint8Array, () => Unit, Option[PanelStackItem[FileListState]]]
    val plugin = new FileListPlugin {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("file 1")))
    )

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val nonFSActions = new MockFileListActions(isLocalFSMock = false)
    val nonFSItem = PanelStackItem[FileListState](
      component = "nonFSItem".asInstanceOf[ReactClass],
      dispatch = None,
      actions = Some(nonFSActions),
      state = Some(currState)
    )
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.isActive shouldBe true
      leftStack.push(nonFSItem)
    }
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState] shouldBe nonFSItem
    }
    inside(nonFSItem.actions) { case Some(actions) =>
      actions.isLocalFS shouldBe false
    }
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "enter"
    
    //then
    onTriggerMock.expects(*, *, *).never()

    //when & then
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState] shouldBe nonFSItem
    }

    Succeeded
  }

  it should "dispatch error task if failed open file when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTriggerMock = mockFunction[String, Uint8Array, () => Unit, Option[PanelStackItem[FileListState]]]
    val plugin = new FileListPlugin {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val fileItem = FileListItem("file 1")
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(fileItem))
    val currState = FileListState(currDir = currDir)

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val actions = new Actions
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.isActive shouldBe true
      leftStack.update[FileListState](_.copy(actions = Some(actions.actions), state = Some(currState)))
    }
    val fsItem = inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState]
    }
    fsItem.state shouldBe Some(currState)
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "enter"
    val expectedError = new Exception("test error")
    
    //then
    actions.readFile.expects(List(currDir.path), fileItem, 0.0).returning(Future.failed(expectedError))
    var openF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case FileListTaskAction(FutureTask("Opening File", future)) =>
        openF = future
      }
    }
    onTriggerMock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    eventually {
      openF should not be null
    }.flatMap(_ => openF.failed).map { ex =>
      ex shouldBe expectedError
      inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
        leftStack.peek[FileListState] shouldBe fsItem
      }
    }
  }

  it should "trigger plugin if file when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTrigger2Mock = mockFunction[String, Uint8Array, () => Unit, Option[PanelStackItem[FileListState]]]
    val onTrigger3Mock = mockFunction[String, Uint8Array, () => Unit, Option[PanelStackItem[FileListState]]]
    val plugin1 = new FileListPlugin {}
    val plugin2 = new FileListPlugin {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
        onTrigger2Mock(filePath, fileHeader, onClose)
      }
    }
    val plugin3 = new FileListPlugin {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
        onTrigger3Mock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin1, plugin2, plugin3))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val fileItem = FileListItem("file 1")
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(fileItem))
    val currState = FileListState(currDir = currDir)

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val actions = new Actions
    val source = new Source
    inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.isActive shouldBe true
      leftStack.update[FileListState](_.copy(actions = Some(actions.actions), state = Some(currState)))
    }
    val fsItem = inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
      leftStack.peek[FileListState]
    }
    fsItem.state shouldBe Some(currState)
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val filePath = path.join("/sub-dir", "file 1")
    val pluginItem = PanelStackItem[FileListState]("pluginPanel".asInstanceOf[ReactClass], None, None, None)
    val keyFull = "enter"
    
    //then
    actions.readFile.expects(List(currDir.path), fileItem, 0.0).returning(Future.successful(source.source))
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      Future.successful(123)
    }
    source.close.expects().returning(Future.unit)
    var onCloseCapture: () => Unit = null
    onTrigger2Mock.expects(filePath, *, *).onCall { (_, fileHeader, onClose) =>
      fileHeader.length shouldBe 123
      onCloseCapture = onClose
      Some(pluginItem)
    }
    onTrigger3Mock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    
    eventually(onCloseCapture should not be null).map { _ =>
      //then
      inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
        val item = leftStack.peek[FileListState]
        item.component shouldBe pluginItem.component
        item.dispatch should not be None
      }

      //when & then
      onCloseCapture()
      inside(findComponentProps(comp, WithPanelStacks)) { case WithPanelStacksProps(leftStack, _, _, _) =>
        leftStack.peek[FileListState] shouldBe fsItem
      }
    }
  }

  it should "trigger and render plugin ui when onKeypress(triggerKey)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onTriggerMock = mockFunction[String, WithPanelStacksProps, Option[ReactClass]]
    val keyFull = "C-p"
    val plugin = new FileListPlugin {
      override val triggerKeys: js.Array[String] = js.Array(keyFull)
      override def onKeyTrigger(key: String, stacks: WithPanelStacksProps): Option[ReactClass] = {
        onTriggerMock(key, stacks)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()

    val renderer = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val stacks = findComponentProps(renderer.root, WithPanelStacks)
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button, _) => button
    }
    val pluginUi = "test_plugin_ui".asInstanceOf[ReactClass]
    
    //then
    onTriggerMock.expects(keyFull, stacks).returning(Some(pluginUi))

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    inside(findComponents(renderer.root, pluginUi)) {
      case List(uiComp) =>
        var resOnClose: js.Function0[Unit] = null
        assertNativeComponent(uiComp, <(pluginUi)(^.assertPlain[FileListPluginUiProps](inside(_) {
          case FileListPluginUiProps(onClose) =>
            resOnClose = onClose
        }))())

        //when
        resOnClose()
        
        //then
        findComponents(renderer.root, pluginUi) should be (empty)
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
      case WithPanelStacksProps(leftStack, _, rightStack, _) =>
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
        <(bottomMenuComp())(^.wrapped := BottomMenuProps(menuItems))()
      ),

      <(fileListPopups).empty,
      <(fsPopups).empty
    ))
  }
}
