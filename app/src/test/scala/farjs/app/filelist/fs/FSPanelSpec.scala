package farjs.app.filelist.fs

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.fs.FSPanel._
import farjs.app.filelist.zip.ZipCreatePopupProps
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.collection.immutable.ListSet
import scala.concurrent.Future

class FSPanelSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")
  FSPanel.fsFreeSpaceComp = mockUiComponent("FSFreeSpace")
  FSPanel.fsService = new FsService().fsService
  FSPanel.zipCreatePopup = mockUiComponent("ZipCreatePopup")

  //noinspection TypeAnnotation
  class Actions {
    val readDir = mockFunction[Option[String], String, Future[FileListDir]]

    val actions = new MockFileListActions(
      readDirMock = readDir
    )
  }

  //noinspection TypeAnnotation
  class FsService {
    val openItem = mockFunction[String, String, Future[Unit]]

    val fsService = new MockFSService(
      openItemMock = openItem
    )
  }

  it should "return false when onKeypress(unknown key)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    FSPanel.fsService = new FsService().fsService
    val props = FileListPanelProps(dispatch, actions, FileListState())
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false
  }

  it should "dispatch action when onKeypress(M-o)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val fsService = new FsService()
    FSPanel.fsService = fsService.fsService
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val comp = testRender(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    fsService.openItem.expects("/sub-dir", "item 1").returning(Future.unit)
    var resultAction: Any = null
    dispatch.expects(*).onCall { action: Any =>
      resultAction = action
    }

    //when & then
    panelProps.onKeypress(null, "M-o") shouldBe true

    inside(resultAction) { case FileListTaskAction(FutureTask("Opening default app", future)) =>
      future.map(_ => Succeeded)
    }
  }

  it should "not render ZipCreatePopup if .. when onKeypress(S-f7)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val renderer = createTestRenderer(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)
    
    //when & then
    panelProps.onKeypress(null, "S-f7") shouldBe true

    //then
    findComponents(renderer.root, zipCreatePopup()) should be (empty)
  }

  it should "render ZipCreatePopup if curr item when onKeypress(S-f7)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    ))
    val renderer = createTestRenderer(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)
    
    //when & then
    panelProps.onKeypress(null, "S-f7") shouldBe true

    //then
    inside(findComponentProps(renderer.root, zipCreatePopup)) {
      case ZipCreatePopupProps(zipName, _, onCancel) =>
        zipName shouldBe "item 1.zip"
        
        //when
        onCancel()
        
        //then
        findComponents(renderer.root, zipCreatePopup()) should be (empty)
    }
  }

  it should "render ZipCreatePopup and call addToZip when onKeypress(S-f7)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPanelProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      selectedNames = ListSet("item 3", "item 2")
    ))
    val addToZip = mockFunction[String, String, Set[String], Future[Unit]]
    FSPanel.addToZip = addToZip
    val renderer = createTestRenderer(<(FSPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(renderer.root, fileListPanelComp)
    
    //when & then
    panelProps.onKeypress(null, "S-f7") shouldBe true

    //then
    inside(findComponentProps(renderer.root, zipCreatePopup)) {
      case ZipCreatePopupProps(zipName, onAdd, _) =>
        zipName shouldBe "item 3.zip"

        //given
        val zipFile = "test.zip"
        val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
          FileListItem("file 1")
        ))
        
        //then
        addToZip.expects(zipFile, props.state.currDir.path, ListSet("item 3", "item 2"))
          .returning(Future.unit)
        dispatch.expects(FileListParamsChangedAction(
          offset = 0,
          index = 1,
          selectedNames = Set.empty
        ))
        actions.readDir.expects(None, props.state.currDir.path).returning(Future.successful(updatedDir))
        dispatch.expects(FileListItemCreatedAction(zipFile, updatedDir))
        var resultAction: Any = null
        dispatch.expects(*).onCall { action: Any =>
          resultAction = action
        }
        
        //when
        onAdd(zipFile)
        
        //then
        findComponents(renderer.root, zipCreatePopup()) should be (empty)
        inside(resultAction) { case FileListTaskAction(FutureTask("Creating zip archive", future)) =>
          future.map(_ => Succeeded)
        }
    }
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    
    //when
    val result = createTestRenderer(<(FSPanel())(^.wrapped := props)()).root

    //then
    assertComponents(result.children, List(
      <(fileListPanelComp())(^.assertWrapped(inside(_) {
        case FileListPanelProps(resDispatch, resActions, resState, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          resState shouldBe state
      }))(),

      <(fsFreeSpaceComp())(^.assertWrapped(inside(_) {
        case FSFreeSpaceProps(resDispatch, currDir) =>
          resDispatch shouldBe dispatch
          currDir shouldBe props.state.currDir
      }))(),
    ))
  }
}
