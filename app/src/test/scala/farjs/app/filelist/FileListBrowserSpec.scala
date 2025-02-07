package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.filelist._
import farjs.filelist.api._
import farjs.filelist.stack._
import farjs.fs.FSPlugin
import farjs.ui.menu.BottomMenuProps
import farjs.ui.task.{Task, TaskAction}
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
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

  it should "not activate left stack if already active when onFocus in left panel" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
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

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
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

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
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
    val dispatch = mockFunction[js.Any, Unit]
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

  it should "not trigger plugin if dir when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onTriggerMock = mockFunction[String, Uint8Array, js.Function0[Unit], js.Promise[js.UndefOr[PanelStackItem[FileListState]]]]
    val plugin = new FileListPlugin(js.Array()) {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
        
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("dir 1", isDir = true)))
    )

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
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
    
    //then
    onTriggerMock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    Future.unit.map { _ =>
      inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
        case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
          leftStack.peek[FileListState]() shouldBe fsItem
      }
    }
  }

  it should "not trigger plugin if not local FS when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onTriggerMock = mockFunction[String, Uint8Array, js.Function0[Unit], js.Promise[js.UndefOr[PanelStackItem[FileListState]]]]
    val plugin = new FileListPlugin(js.Array()) {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val currState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem("file 1")))
    )

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val nonFSActions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val nonFSItem = PanelStackItem[FileListState](
      component = "nonFSItem".asInstanceOf[ReactClass],
      dispatch = js.undefined,
      actions = nonFSActions,
      state = currState
    )
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.isActive shouldBe true
        leftStack.push(nonFSItem)
    }
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.peek[FileListState]() shouldBe nonFSItem
    }
    inside(nonFSItem.actions.toOption) { case Some(actions) =>
      actions.api.isLocal shouldBe false
    }
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val keyFull = "enter"
    
    //then
    onTriggerMock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    
    //then
    Future.unit.map { _ =>
      inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
        case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
          leftStack.peek[FileListState]() shouldBe nonFSItem
      }
    }
  }

  it should "dispatch error task if failed open file when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onTriggerMock = mockFunction[String, Uint8Array, js.Function0[Unit], js.Promise[js.UndefOr[PanelStackItem[FileListState]]]]
    val plugin = new FileListPlugin(js.Array()) {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
        onTriggerMock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val fileItem = FileListItem("file 1")
    val currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(fileItem))
    val currState = FileListState(currDir = currDir)

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val actions = new Actions
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.isActive shouldBe true
        leftStack.update[FileListState](PanelStackItem.copy(_)(actions = actions.actions, state = currState))
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
    val expectedError = js.Error("test error")
    
    //then
    actions.readFile.expects(currDir.path, fileItem, 0.0).returning(js.Promise.reject(expectedError))
    var openF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case TaskAction(Task("Opening File", future)) =>
        openF = future
      }
      ()
    }
    onTriggerMock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //then
    eventually {
      openF should not be null
    }.flatMap(_ => openF.failed).map { ex =>
      ex shouldBe js.JavaScriptException(expectedError)
      inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
        case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
          leftStack.peek[FileListState]() shouldBe fsItem
      }
    }
  }

  it should "trigger plugin if file when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onTrigger2Mock = mockFunction[String, Uint8Array, () => Unit, js.Promise[js.UndefOr[PanelStackItem[FileListState]]]]
    val onTrigger3Mock = mockFunction[String, Uint8Array, () => Unit, js.Promise[js.UndefOr[PanelStackItem[FileListState]]]]
    val plugin1 = new FileListPlugin(js.Array()) {}
    val plugin2 = new FileListPlugin(js.Array()) {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
        onTrigger2Mock(filePath, fileHeader, onClose)
      }
    }
    val plugin3 = new FileListPlugin(js.Array()) {
      override def onFileTrigger(filePath: String,
                                 fileHeader: Uint8Array,
                                 onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
        onTrigger3Mock(filePath, fileHeader, onClose)
      }
    }
    val props = FileListBrowserProps(dispatch, plugins = List(plugin1, plugin2, plugin3))
    val focusMock = mockFunction[Unit]
    val buttonMock = literal("focus" -> focusMock)
    focusMock.expects()
    val fileItem = FileListItem("file 1")
    val currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(fileItem))
    val currState = FileListState(currDir = currDir)

    val comp = testRender(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.button.name.asInstanceOf[js.Any]) buttonMock
      else null
    })
    val actions = new Actions
    val source = new Source
    inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.isActive shouldBe true
        leftStack.update[FileListState](PanelStackItem.copy(_)(actions = actions.actions, state = currState))
    }
    val fsItem = inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
      case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
        leftStack.peek[FileListState]()
    }
    fsItem.state shouldBe currState
    val button = inside(findComponents(comp, <.button.name)) {
      case List(button, _) => button
    }
    val filePath = path.join("/sub-dir", "file 1")
    val pluginItem = PanelStackItem[FileListState]("pluginPanel".asInstanceOf[ReactClass])
    val keyFull = "enter"
    
    //then
    actions.readFile.expects(currDir.path, fileItem, 0.0).returning(js.Promise.resolve[FileSource](source.source))
    source.readNextBytes.expects(*).onCall { buff: Uint8Array =>
      buff.length shouldBe (64 * 1024)
      js.Promise.resolve[Int](123)
    }
    source.close.expects().returning(js.Promise.resolve[Unit](()))
    var onCloseCapture: () => Unit = null
    onTrigger2Mock.expects(filePath, *, *).onCall { (_, fileHeader, onClose) =>
      fileHeader.length shouldBe 123
      onCloseCapture = onClose
      js.Promise.resolve[js.UndefOr[PanelStackItem[FileListState]]](pluginItem)
    }
    onTrigger3Mock.expects(*, *, *).never()

    //when
    button.props.onKeypress(null, literal(full = keyFull).asInstanceOf[KeyboardKey])
    
    eventually(onCloseCapture should not be null).flatMap { _ =>
      //then
      inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
        case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
          val item = leftStack.peek[FileListState]()
          item.component shouldBe pluginItem.component
          item.dispatch should not be None
      }

      //when
      onCloseCapture()
      
      //then
      Future.unit.map { _ =>
        inside(findComponents(comp, WithStacks).map(_.props.asInstanceOf[WithStacksProps])) {
          case List(WithStacksProps(WithStacksData(leftStack, _), _)) =>
            leftStack.peek[FileListState]() shouldBe fsItem
        }
      }
    }
  }

  it should "trigger and render plugin ui when onKeypress(triggerKey)" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val onTriggerMock = mockFunction[String, WithStacksProps, js.UndefOr[js.Dynamic], js.Promise[js.UndefOr[ReactClass]]]
    val keyFull = "C-p"
    val plugin = new FileListPlugin(js.Array()) {
      override val triggerKeys: js.Array[String] = js.Array(keyFull)
      override def onKeyTrigger(key: String, stacks: WithStacksProps, data: js.UndefOr[js.Dynamic]): js.Promise[js.UndefOr[ReactClass]] = {
        onTriggerMock(key, stacks, data)
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
    val stacks = findComponents(renderer.root, WithStacks).map(_.props.asInstanceOf[WithStacksProps]).head
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button, _) => button
    }
    val pluginUi = "test_plugin_ui".asInstanceOf[ReactClass]
    val data: js.UndefOr[js.Dynamic] = js.Dynamic.literal()
    
    //then
    onTriggerMock.expects(keyFull, *, data).onCall { (_, resStacks, _) =>
      WithStacksSpec.assertStacks(resStacks, stacks)
      js.Promise.resolve[js.UndefOr[ReactClass]](pluginUi)
    }

    //when
    button.props.onKeypress(null, literal(full = keyFull, data = data).asInstanceOf[KeyboardKey])

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

  it should "dispatch error task if failed open plugin when onKeypress(triggerKey)" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onTriggerMock = mockFunction[String, WithStacksProps, js.UndefOr[js.Dynamic], js.Promise[js.UndefOr[ReactClass]]]
    val keyFull = "C-p"
    val plugin = new FileListPlugin(js.Array()) {
      override val triggerKeys: js.Array[String] = js.Array(keyFull)
      override def onKeyTrigger(key: String, stacks: WithStacksProps, data: js.UndefOr[js.Dynamic]): js.Promise[js.UndefOr[ReactClass]] = {
        onTriggerMock(key, stacks, data)
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
    val stacks = findComponents(renderer.root, WithStacks).map(_.props.asInstanceOf[WithStacksProps]).head
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button, _) => button
    }
    val data: js.UndefOr[js.Dynamic] = js.Dynamic.literal()
    val expectedError = new Exception("test error")
    
    //then
    onTriggerMock.expects(keyFull, *, data).onCall { (_, resStacks, _) =>
      WithStacksSpec.assertStacks(resStacks, stacks)
      js.Promise.reject(expectedError)
    }

    var openF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case TaskAction(Task("Opening Plugin", future)) =>
        openF = future
      }
      ()
    }

    //when
    button.props.onKeypress(null, literal(full = keyFull, data = data).asInstanceOf[KeyboardKey])

    //then
    eventually {
      openF should not be null
    }.flatMap(_ => openF.failed).map { ex =>
      ex shouldBe expectedError
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
