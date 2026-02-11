package farjs.archiver.zip

import farjs.archiver._
import farjs.archiver.zip.ZipPanel._
import farjs.archiver.zip.ZipPanelSpec.withContext
import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.{assertFileListDirChangedAction, assertFileListDiskSpaceUpdatedAction}
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.stack._
import farjs.ui.Dispatch
import farjs.ui.popup.{MessageBoxAction, MessageBoxProps}
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.{OptionValues, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class ZipPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils with OptionValues {

  ZipPanel.fileListPanelComp = "FileListPanel".asInstanceOf[ReactClass]
  ZipPanel.addToArchController = "AddToArchController".asInstanceOf[ReactClass]
  ZipPanel.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions(isLocal: Boolean) {
    val updateDir = mockFunction[Dispatch, String, TaskAction]
    val deleteItems = mockFunction[Dispatch, String, js.Array[FileListItem], TaskAction]

    val actions = new MockFileListActions(
      new MockFileListApi(isLocalMock = isLocal),
      updateDirMock = updateDir,
      deleteItemsMock = deleteItems
    )
  }

  private val entriesByParentF = js.Promise.resolve[js.Map[String, js.Array[FileListItem]]](new js.Map[String, js.Array[FileListItem]](js.Array(
    "" -> js.Array[FileListItem](
      ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0),
      ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0)
    ),
    "dir 1" -> js.Array[FileListItem](
      ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0)
    ),
    "dir 1/dir 2" -> js.Array[FileListItem](
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0)
    )
  )))

  it should "return false when onKeypress(unknown key)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, "unknown") shouldBe false
  }

  it should "call onClose if root dir when onKeypress(C-pageup)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[js.Any, Unit]
    val fsActions = new Actions(isLocal = true)
    val fsState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array()))
    val leftStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass]),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch: js.Function1[js.Any, Unit], fsActions.actions, fsState)
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val updateAction = TaskAction(Task("Updating...", Future.successful(updatedDir)))

    //then
    fsActions.updateDir.expects(*, fsState.currDir.path).returning(updateAction)
    fsDispatch.expects(updateAction)
    onClose.expects()

    //when & then
    panelProps.onKeypress.toOption.value(null, "C-pageup") shouldBe true

    Succeeded
  }

  it should "call onClose if on .. in root dir when onKeypress(enter|C-pagedown)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[js.Any, Unit]
    val fsActions = new Actions(isLocal = true)
    val fsState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array()))
    val leftStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass])
    ), null)
    val rightStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass]),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch: js.Function1[js.Any, Unit], fsActions.actions, fsState)
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val updateAction = TaskAction(Task("Updating...", Future.successful(updatedDir)))

    //then
    fsActions.updateDir.expects(*, fsState.currDir.path).returning(updateAction).twice()
    fsDispatch.expects(updateAction).twice()
    onClose.expects().twice()

    //when & then
    panelProps.onKeypress.toOption.value(null, "enter") shouldBe true
    panelProps.onKeypress.toOption.value(null, "C-pagedown") shouldBe true

    Succeeded
  }

  it should "not call onClose if not on .. in root dir when onKeypress(enter|C-pagedown)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //then
    onClose.expects().never()

    //when & then
    panelProps.onKeypress.toOption.value(null, "enter") shouldBe false
    panelProps.onKeypress.toOption.value(null, "C-pagedown") shouldBe false

    Succeeded
  }

  it should "not render AddToArchController if non-local FS when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListCopy) shouldBe false

    //then
    findComponents(comp, addToArchController) should be (empty)
  }

  it should "not render AddToArchController if .. when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(comp, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListCopy) shouldBe false

    //then
    findComponents(comp, addToArchController) should be (empty)
  }

  it should "render AddToArchController and handle onCancel when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val items = List(FileListItem("file 1"))
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem.up) ++ items)
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(renderer.root, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    val controllerProps = inside(findComponents(renderer.root, addToArchController)) {
      case List(c) => c.props.asInstanceOf[AddToArchControllerProps]
    }
    inside(controllerProps) {
      case AddToArchControllerProps(resDispatch, resActions, state, archName, archType, archAction, _, resItems, _, onCancel) =>
        resDispatch shouldBe fsDispatch
        resActions shouldBe fsActions
        state shouldBe fsState
        archName shouldBe "dir/file.zip"
        archType shouldBe "zip"
        archAction shouldBe AddToArchAction.Copy
        resItems.toList shouldBe items

        //when
        onCancel()

        //then
        findComponents(renderer.root, addToArchController) should be (empty)
    }
  }

  it should "render AddToArchController and handle onComplete when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions(isLocal = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val items = List(
      FileListItem("item 2"),
      FileListItem("item 3")
    )
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ) ++ items),
      selectedNames = js.Set("item 3", "item 2")
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(renderer.root, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    val controllerProps = inside(findComponents(renderer.root, addToArchController)) {
      case List(c) => c.props.asInstanceOf[AddToArchControllerProps]
    }
    inside(controllerProps) {
      case AddToArchControllerProps(resDispatch, resActions, state, archName, archType, archAction, _, resItems, onComplete, _) =>
        resDispatch shouldBe fsDispatch
        resActions shouldBe fsActions
        state shouldBe fsState
        archName shouldBe "dir/file.zip"
        archType shouldBe "zip"
        archAction shouldBe AddToArchAction.Copy
        resItems.toList shouldBe items

        //given
        val zipFile = "test.zip"
        val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
          FileListItem("file 1")
        ))
        val updateAction = TaskAction(Task("Updating...", Future.successful(updatedDir)))

        //then
        actions.updateDir.expects(*, props.state.currDir.path).returning(updateAction)
        dispatch.expects(updateAction)

        //when
        onComplete(zipFile)

        //then
        findComponents(renderer.root, addToArchController) should be (empty)
        updateAction.task.result.toFuture.map(_ => Succeeded)
    }
  }

  it should "render AddToArchController and handle onComplete when onKeypress(onFileListMove)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions(isLocal = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[js.Any, Unit]
    val fsActions = new Actions(isLocal = true)
    val items = List(
      FileListItem("item 2"),
      FileListItem("item 3")
    )
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ) ++ items),
      selectedNames = js.Set("item 3", "item 2")
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch: js.Function1[js.Any, Unit], fsActions.actions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(renderer.root, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListMove) shouldBe true

    //then
    val controllerProps = inside(findComponents(renderer.root, addToArchController)) {
      case List(c) => c.props.asInstanceOf[AddToArchControllerProps]
    }
    inside(controllerProps) {
      case AddToArchControllerProps(_, resActions, state, archName, archType, archAction, _, resItems, onComplete, _) =>
        resActions shouldBe fsActions.actions
        state shouldBe fsState
        archName shouldBe "dir/file.zip"
        archType shouldBe "zip"
        archAction shouldBe AddToArchAction.Move
        resItems.toList shouldBe items

        //given
        val zipFile = "test.zip"
        val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
          FileListItem("file 1")
        ))
        val updateAction = TaskAction(Task("Updating...", Future.successful(updatedDir)))
        val deleteAction = TaskAction(Task("Deleting...", Future.unit))

        //then
        actions.updateDir.expects(*, props.state.currDir.path).returning(updateAction)
        dispatch.expects(updateAction)
        fsActions.deleteItems.expects(*, fsState.currDir.path, *).onCall { (_, _, resItems) =>
          resItems.toList shouldBe items
          deleteAction
        }
        fsDispatch.expects(deleteAction)

        //when
        onComplete(zipFile)

        //then
        findComponents(renderer.root, addToArchController) should be (empty)
        for {
          _ <- updateAction.task.result.toFuture
          _ <- deleteAction.task.result.toFuture
        } yield Succeeded
    }
  }

  it should "render MessageBox if non-root dir when onKeypress(onFileListCopy|Move)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions(isLocal = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("zip://filePath.zip/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )
    val panelProps = inside(findComponents(renderer.root, fileListPanelComp)) {
      case List(c) => c.props.asInstanceOf[FileListPanelProps]
    }

    //when & then
    panelProps.onKeypress.toOption.value(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    findComponents(renderer.root, addToArchController) should be (empty)
    val msgBox = inside(findComponents(renderer.root, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }
    inside(msgBox) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Warning"
        message shouldBe "Items can only be added to zip root."
        inside(resActions.toList) {
          case List(MessageBoxAction("OK", onAction, true)) =>
            //when
            onAction()

            //then
            findComponents(renderer.root, messageBoxComp) should be (empty)
        }
        style shouldBe DefaultTheme.popup.regular
    }
  }

  it should "dispatch action with empty dir if failed entries future" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, js.Promise.reject(JavaScriptException(js.Error("test"))), onClose)
    val dir = FileListDir(rootPath, isRoot = false, js.Array())
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) {
        case TaskAction(Task("Reading zip archive", future)) =>
          actionF = future
      }
      ()
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDirChangedAction(action,
        FileListDirChangedAction(FileListItem.currDir.name, dir))
      ()
    }
    
    //when
    testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )

    //then
    for {
      _ <- eventually(actionF should not be null)
      _ <- actionF.failed
    } yield Succeeded
  }

  it should "not dispatch actions if state is not empty when mount" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val state = FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up
      ))
    )
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, js.Promise.resolve[js.Map[String, js.Array[FileListItem]]](new js.Map[String, js.Array[FileListItem]]()), onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)
    
    //then
    dispatch.expects(*).never()
    
    //when
    testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )

    //then
    Succeeded
  }

  it should "render initial component and dispatch actions" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions(new MockFileListApi(isLocalMock = false))
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val fsActions = new MockFileListActions
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], fsDispatch, fsActions, fsState)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("zipComp".asInstanceOf[ReactClass])
    ), null)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) {
        case TaskAction(Task("Reading zip archive", future)) =>
          actionF = future
      }
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDiskSpaceUpdatedAction(action, FileListDiskSpaceUpdatedAction(7.0))
      ()
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDirChangedAction(action,
        FileListDirChangedAction(FileListItem.currDir.name, FileListDir(
          path = rootPath,
          isRoot = false,
          items = js.Array(
            ZipEntry("", "dir 1", isDir = true, size = 0, datetimeMs = 1.0),
            ZipEntry("", "file 1", isDir = false, size = 2.0, datetimeMs = 3.0)
          )
        ))
      )
      ()
    }
    
    //when
    val result = testRender(
      withContext(<(zipPanel())(^.plain := props)(), leftStack, rightStack)
    )

    //then
    assertNativeComponent(result, <(fileListPanelComp)(^.assertPlain[FileListPanelProps](inside(_) {
      case FileListPanelProps(_, resActions, resState, _) =>
        resActions shouldBe actions
        resState shouldBe state
    }))())
    for {
      _ <- eventually(actionF should not be null)
      _ <- actionF
    } yield Succeeded
  }
}

object ZipPanelSpec {

  def withContext(element: ReactElement, leftStack: PanelStack, rightStack: PanelStack): ReactElement = {
    <(WithStacks.Context.Provider)(^.contextValue := WithStacksProps(
      left = WithStacksData(leftStack, null),
      right = WithStacksData(rightStack, null)
    ))(
      withThemeContext(element)
    )
  }
}
