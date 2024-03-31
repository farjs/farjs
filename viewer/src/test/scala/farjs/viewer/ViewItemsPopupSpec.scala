package farjs.viewer

import farjs.filelist.FileListActions.{FileListDirUpdatedAction, FileListTaskAction}
import farjs.filelist._
import farjs.filelist.api.FileListDirSpec.assertFileListDir
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.popup.StatusPopupProps
import farjs.viewer.ViewItemsPopup._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

class ViewItemsPopupSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  ViewItemsPopup.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean, Future[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "dispatch action with calculated items sizes" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val dir1 = FileListItem("dir 1", isDir = true)
    val file1 = FileListItem("file 1", size = 10)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(dir1, file1))
    val state = FileListState(currDir = currDir, isActive = true, selectedNames = Set("dir 1", "file 1"))
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      p.future.map { res =>
        res shouldBe true
        onNextDir("/path", List(
          FileListItem("dir 2", isDir = true),
          FileListItem("file 2", size = 123)
        )) shouldBe true
        res
      }
    }
    val viewItemsPopup = new ViewItemsPopup(FileListData(dispatch, actions.actions, state))
    val renderer = createTestRenderer(<(viewItemsPopup())(^.plain := props)())
    
    eventually {
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      var resAction: FileListDirUpdatedAction = null

      //then
      onClose.expects()
      dispatch.expects(*).onCall { action: Any =>
        inside(action) { case action: FileListDirUpdatedAction =>
          resAction = action
        }
      }

      //when
      p.success(true)

      //then
      eventually {
        resAction should not be null
        inside(resAction) {
          case FileListDirUpdatedAction(resDir) =>
            assertFileListDir(resDir, FileListDir.copy(currDir)(items = js.Array(
              dir1.copy(size = 123),
              file1.copy(size = 10)
            )))
        }
      }
    }
  }

  it should "handle cancel action when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      p.future.map { res =>
        res shouldBe false
        onNextDir("/path", List(
          FileListItem("dir 2", isDir = true),
          FileListItem("file 2", size = 123)
        )) shouldBe false
        res
      }
    }
    val viewItemsPopup = new ViewItemsPopup(FileListData(dispatch, actions.actions, state))
    val renderer = createTestRenderer(<(viewItemsPopup())(^.plain := props)())

    eventually {
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }

      //when
      popup.onClose.foreach(_.apply())

      //then
      onClose.expects()
      dispatch.expects(*).never()
      p.success(false)
      p.future.map(_ => Succeeded)
    }
  }

  it should "dispatch actions when failure" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)
    val viewItemsPopup = new ViewItemsPopup(FileListData(dispatch, actions.actions, state))
    val renderer = createTestRenderer(<(viewItemsPopup())(^.plain := props)())
    inside(findComponents(renderer.root, statusPopupComp)) {
      case List(_) =>
    }
    var resultF: Future[_] = null
    val error = new Exception("test error")

    //then
    onClose.expects()
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[FileListTaskAction]) { case action: FileListTaskAction =>
        resultF = action.task.result.toFuture
      }
    }

    //when
    p.failure(error)
    
    //then
    eventually {
      resultF should not be null
    }.flatMap(_ => resultF.failed).map { ex =>
      ex shouldBe error
    }
  }

  it should "render StatusPopup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val viewItemsPopup = new ViewItemsPopup(FileListData(dispatch, actions.actions, state))

    //then
    onClose.expects()
    dispatch.expects(*).onCall { action: Any =>
      inside(action) {
        case FileListDirUpdatedAction(resDir) =>
          assertFileListDir(resDir, state.currDir)
      }
    }

    //when
    val result = testRender(<(viewItemsPopup())(^.plain := props)())

    //then
    assertNativeComponent(result, <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
      case StatusPopupProps(text, title, onClose) =>
        text shouldBe "Scanning the folder\n"
        title shouldBe "View"
        onClose.isDefined shouldBe true
    }))())
  }
}
