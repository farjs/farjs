package farjs.archiver.zip

import farjs.archiver._
import farjs.archiver.zip.ZipPanel._
import farjs.archiver.zip.ZipPanelSpec.withContext
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.FileListDirSpec.assertFileListDir
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.ui.Dispatch
import farjs.ui.popup.{MessageBoxAction, MessageBoxProps}
import farjs.ui.task.Task
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class ZipPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ZipPanel.fileListPanelComp = mockUiComponent("FileListPanel")
  ZipPanel.addToArchController = mockUiComponent("AddToArchController")
  ZipPanel.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions(isLocalFS: Boolean) {
    val updateDir = mockFunction[Dispatch, String, FileListDirUpdateAction]
    val deleteAction = mockFunction[Dispatch, String, Seq[FileListItem], FileListTaskAction]

    val actions = new MockFileListActions(
      isLocalFSMock = isLocalFS,
      updateDirMock = updateDir,
      deleteActionMock = deleteAction
    )
  }

  private val entriesByParentF = Future.successful(Map(
    "" -> List(
      ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0),
      ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0)
    ),
    "dir 1" -> List(
      ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0)
    ),
    "dir 1/dir 2" -> List(
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0)
    )
  ))

  it should "return false when onKeypress(unknown key)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false
  }

  it should "call onClose if root dir when onKeypress(C-pageup)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new Actions(isLocalFS = true)
    val fsState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array()))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions.actions), Some(fsState))
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val updateAction = FileListDirUpdateAction(Task("Updating...", Future.successful(updatedDir)))

    //then
    fsActions.updateDir.expects(fsDispatch, fsState.currDir.path).returning(updateAction)
    fsDispatch.expects(updateAction)
    onClose.expects()

    //when & then
    panelProps.onKeypress(null, "C-pageup") shouldBe true

    Succeeded
  }

  it should "call onClose if on .. in root dir when onKeypress(enter|C-pagedown)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new Actions(isLocalFS = true)
    val fsState = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array()))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions.actions), Some(fsState))
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val updateAction = FileListDirUpdateAction(Task("Updating...", Future.successful(updatedDir)))

    //then
    fsActions.updateDir.expects(fsDispatch, fsState.currDir.path).returning(updateAction).twice()
    fsDispatch.expects(updateAction).twice()
    onClose.expects().twice()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe true
    panelProps.onKeypress(null, "C-pagedown") shouldBe true

    Succeeded
  }

  it should "not call onClose if not on .. in root dir when onKeypress(enter|C-pagedown)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects().never()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe false
    panelProps.onKeypress(null, "C-pagedown") shouldBe false

    Succeeded
  }

  it should "not render AddToArchController if non-local FS when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = false)
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListCopy) shouldBe false

    //then
    findProps(comp, addToArchController) should be (empty)
  }

  it should "not render AddToArchController if .. when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val comp = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListCopy) shouldBe false

    //then
    findProps(comp, addToArchController) should be (empty)
  }

  it should "render AddToArchController and handle onCancel when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val items = List(FileListItem("file 1"))
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(FileListItem.up) ++ items)
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    inside(findComponentProps(renderer.root, addToArchController)) {
      case AddToArchControllerProps(resDispatch, resActions, state, zipName, resItems, action, _, onCancel) =>
        resDispatch shouldBe fsDispatch
        resActions shouldBe fsActions
        state shouldBe fsState
        zipName shouldBe "dir/file.zip"
        resItems shouldBe items
        action shouldBe AddToArchAction.Copy

        //when
        onCancel()

        //then
        findComponents(renderer.root, addToArchController()) should be (empty)
    }
  }

  it should "render AddToArchController and handle onComplete when onKeypress(onFileListCopy)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions(isLocalFS = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
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
      selectedNames = Set("item 3", "item 2")
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    inside(findComponentProps(renderer.root, addToArchController)) {
      case AddToArchControllerProps(resDispatch, resActions, state, zipName, resItems, action, onComplete, _) =>
        resDispatch shouldBe fsDispatch
        resActions shouldBe fsActions
        state shouldBe fsState
        zipName shouldBe "dir/file.zip"
        resItems shouldBe items
        action shouldBe AddToArchAction.Copy

        //given
        val zipFile = "test.zip"
        val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
          FileListItem("file 1")
        ))
        val updateAction = FileListDirUpdateAction(Task("Updating...", Future.successful(updatedDir)))

        //then
        actions.updateDir.expects(dispatch, props.state.currDir.path).returning(updateAction)
        dispatch.expects(updateAction)

        //when
        onComplete(zipFile)

        //then
        findComponents(renderer.root, addToArchController()) should be (empty)
        updateAction.task.result.toFuture.map(_ => Succeeded)
    }
  }

  it should "render AddToArchController and handle onComplete when onKeypress(onFileListMove)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions(isLocalFS = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new Actions(isLocalFS = true)
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
      selectedNames = Set("item 3", "item 2")
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions.actions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListMove) shouldBe true

    //then
    inside(findComponentProps(renderer.root, addToArchController)) {
      case AddToArchControllerProps(resDispatch, resActions, state, zipName, resItems, action, onComplete, _) =>
        resDispatch shouldBe fsDispatch
        resActions shouldBe fsActions.actions
        state shouldBe fsState
        zipName shouldBe "dir/file.zip"
        resItems shouldBe items
        action shouldBe AddToArchAction.Move

        //given
        val zipFile = "test.zip"
        val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
          FileListItem("file 1")
        ))
        val updateAction = FileListDirUpdateAction(Task("Updating...", Future.successful(updatedDir)))
        val deleteAction = FileListTaskAction(Task("Deleting...", Future.unit))

        //then
        actions.updateDir.expects(dispatch, props.state.currDir.path).returning(updateAction)
        dispatch.expects(updateAction)
        fsActions.deleteAction.expects(fsDispatch, fsState.currDir.path, items).returning(deleteAction)
        fsDispatch.expects(deleteAction)

        //when
        onComplete(zipFile)

        //then
        findComponents(renderer.root, addToArchController()) should be (empty)
        for {
          _ <- updateAction.task.result.toFuture
          _ <- deleteAction.task.result.toFuture
        } yield Succeeded
    }
  }

  it should "render MessageBox if non-root dir when onKeypress(onFileListCopy|Move)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions(isLocalFS = false)
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("zip://filePath.zip/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ))
    )
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    dispatch.expects(*).never()
    val renderer = createTestRenderer(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, FileListEvent.onFileListCopy) shouldBe true

    //then
    findProps(renderer.root, addToArchController) should be (empty)
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
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, Future.failed(new Exception("test")), onClose)
    val dir = FileListDir(rootPath, isRoot = false, js.Array())
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[FileListTaskAction]) {
        case FileListTaskAction(Task("Reading zip archive", future)) =>
          actionF = future
      }
    }
    dispatch.expects(*).onCall { action: Any =>
      inside(action) {
        case FileListDirChangedAction(FileListItem.currDir.name, resDir) =>
          assertFileListDir(resDir, dir)
      }
    }
    
    //when
    testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
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
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val state = FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = js.Array(
        FileListItem.up
      ))
    )
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, Future.successful(Map.empty), onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    
    //then
    dispatch.expects(*).never()
    
    //when
    testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    Succeeded
  }

  it should "render initial component and dispatch actions" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions(isLocalFSMock = false)
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel("dir/file.zip", rootPath, entriesByParentF, onClose)
    val fsDispatch = mockFunction[Any, Any]
    val fsActions = new MockFileListActions(isLocalFSMock = true)
    val fsState = FileListState()
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fsDispatch), Some(fsActions), Some(fsState))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("zipComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[FileListTaskAction]) {
        case FileListTaskAction(Task("Reading zip archive", future)) =>
          actionF = future
      }
    }
    dispatch.expects(FileListDiskSpaceUpdatedAction(7.0))
    dispatch.expects(*).onCall { action: Any =>
      inside(action) {
        case FileListDirChangedAction(FileListItem.currDir.name, resDir) =>
          assertFileListDir(resDir, FileListDir(
            path = rootPath,
            isRoot = false,
            items = js.Array(
              FileListItem("dir 1", isDir = true, mtimeMs = 1.0),
              FileListItem("file 1", size = 2.0, mtimeMs = 3.0)
            )
          ))
      }
    }
    
    //when
    val result = testRender(
      withContext(<(zipPanel())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }
    for {
      _ <- eventually(actionF should not be null)
      _ <- actionF
    } yield Succeeded
  }
}

object ZipPanelSpec {

  def withContext(element: ReactElement, leftStack: PanelStack, rightStack: PanelStack): ReactElement = {
    <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(
      leftStack = leftStack,
      leftInput = null,
      rightStack = rightStack,
      rightInput = null
    ))(
      withThemeContext(element)
    )
  }
}
