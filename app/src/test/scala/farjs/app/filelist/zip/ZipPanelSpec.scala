package farjs.app.filelist.zip

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class ZipPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ZipPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  private val entriesF = Future.successful(List(
    ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0),
    ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0),
    ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0),
    ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0)
  ))

  it should "return false when onKeypress(unknown key)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)

    dispatch.expects(*).never()
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false

    Succeeded
  }

  it should "call onClose if root dir when onKeypress(C-pageup)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)

    dispatch.expects(*).never()
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects()

    //when & then
    panelProps.onKeypress(null, "C-pageup") shouldBe true

    Succeeded
  }

  it should "call onClose if on .. in root dir when onKeypress(C-pagedown)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)

    dispatch.expects(*).never()
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects()

    //when & then
    panelProps.onKeypress(null, "C-pagedown") shouldBe true

    Succeeded
  }

  it should "call onClose if on .. in root dir when onKeypress(enter)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)

    dispatch.expects(*).never()
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe true

    Succeeded
  }

  it should "not call onClose if not on .. in root dir when onKeypress(enter)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)

    dispatch.expects(*).never()
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects().never()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe false

    Succeeded
  }

  it should "dispatch action with empty dir if failed entries future" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, Future.failed(new Exception("test")), onClose)
    val dir = FileListDir(rootPath, isRoot = false, Nil)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action) {
        case FileListTaskAction(FutureTask("Reading zip archive", future)) =>
          actionF = future
      }
    }
    dispatch.expects(FileListDirChangedAction(FileListDir.curr, dir))
    
    //when
    testRender(<(zipPanel())(^.wrapped := props)())

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
    val actions = new MockFileListActions
    val state = FileListState(
      currDir = FileListDir("zip://filePath.zip", isRoot = false, items = List(
        FileListItem.up
      ))
    )
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, Future.successful(Nil), onClose)
    
    //then
    dispatch.expects(*).never()
    
    //when
    testRender(<(zipPanel())(^.wrapped := props)())

    //then
    Succeeded
  }

  it should "render initial component and dispatch actions" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val rootPath = "zip://filePath.zip"
    val zipPanel = new ZipPanel(rootPath, entriesF, onClose)
    
    //then
    var actionF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action) {
        case FileListTaskAction(FutureTask("Reading zip archive", future)) =>
          actionF = future
      }
    }
    dispatch.expects(FileListDiskSpaceUpdatedAction(7.0))
    dispatch.expects(FileListDirChangedAction(FileListDir.curr, FileListDir(
      path = rootPath,
      isRoot = false,
      items = List(
        FileListItem("dir 1", isDir = true, mtimeMs = 1.0),
        FileListItem("file 1", size = 2.0, mtimeMs = 3.0)
      )
    )))
    
    //when
    val result = testRender(<(zipPanel())(^.wrapped := props)())

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
