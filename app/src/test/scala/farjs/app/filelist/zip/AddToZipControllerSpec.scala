package farjs.app.filelist.zip

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.zip.AddToZipController._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.popup.StatusPopupProps
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class AddToZipControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  AddToZipController.addToZipPopup = mockUiComponent("AddToZipPopup")
  AddToZipController.statusPopupComp = mockUiComponent("StatusPopup")

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean, Future[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "dispatch failed task action when api failed" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val items = List(FileListItem("dir 3", isDir = true))
    val onComplete = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipControllerProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2")
      ) ++ items),
      selectedNames = Set("dir 3")
    ), zipName = "new.zip", items, AddToZipAction.Add, onComplete, onCancel)
    val addToZipApi = mockFunction[String, String, Set[String], () => Unit, Future[Unit]]
    AddToZipController.addToZipApi = addToZipApi

    //when
    val renderer = createTestRenderer(<(AddToZipController())(^.wrapped := props)())

    //then
    findComponents(renderer.root, statusPopupComp()) should be (empty)
    inside(findComponentProps(renderer.root, addToZipPopup)) {
      case AddToZipPopupProps(zipName, action, onAction, onCancel) =>
        zipName shouldBe "new.zip"
        action shouldBe AddToZipAction.Add
        onCancel shouldBe props.onCancel

        //given
        val zipFile = "test.zip"

        //then
        val p = Promise[Boolean]()
        actions.scanDirs.expects(props.state.currDir.path, items, *).onCall { (_, _, onNextDir) =>
          onNextDir("/path", List(
            FileListItem("dir 2", isDir = true),
            FileListItem("file 1", size = 123)
          ))
          p.future
        }
        val addToZipF = Future.failed(new Exception("test error"))
        addToZipApi.expects(zipFile, props.state.currDir.path, Set("dir 3"), *).returning(addToZipF)
        onComplete.expects(*).never()
        var resultAction: Any = null
        dispatch.expects(*).onCall { action: Any =>
          resultAction = action
        }

        //when
        onAction(zipFile)

        //then
        findComponents(renderer.root, addToZipPopup()) should be (empty)
        p.success(true)
        eventually {
          findComponents(renderer.root, statusPopupComp()) should be (empty)
        }.flatMap { _ =>
          inside(resultAction) {
            case FileListTaskAction(FutureTask("Add item(s) to zip archive", future)) =>
              future.failed.map(_ => Succeeded)
          }
        }
    }
  }

  it should "render add popup and status popup when api succeeded" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val items = List(FileListItem("dir 3", isDir = true))
    val onComplete = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipControllerProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2")
      ) ++ items),
      selectedNames = Set("dir 3")
    ), zipName = "new.zip", items, AddToZipAction.Add, onComplete, onCancel)
    val addToZipApi = mockFunction[String, String, Set[String], () => Unit, Future[Unit]]
    AddToZipController.addToZipApi = addToZipApi

    //when
    val renderer = createTestRenderer(<(AddToZipController())(^.wrapped := props)())

    //then
    findComponents(renderer.root, statusPopupComp()) should be (empty)
    inside(findComponentProps(renderer.root, addToZipPopup)) {
      case AddToZipPopupProps(zipName, action, onAction, onCancel) =>
        zipName shouldBe "new.zip"
        action shouldBe AddToZipAction.Add
        onCancel shouldBe props.onCancel

        //given
        val zipFile = "test.zip"

        //then
        actions.scanDirs.expects(props.state.currDir.path, items, *).onCall { (_, _, onNextDir) =>
          onNextDir("/path", List(
            FileListItem("dir 2", isDir = true),
            FileListItem("file 1", size = 123)
          ))
          Future.successful(true)
        }
        val p = Promise[Unit]()
        var onNextItemFunc: () => Unit = null
        addToZipApi.expects(zipFile, props.state.currDir.path, Set("dir 3"), *).onCall { (_, _, _, onNextItem) =>
          onNextItemFunc = onNextItem
          p.future
        }
        dispatch.expects(FileListParamsChangedAction(
          offset = 0,
          index = 1,
          selectedNames = Set.empty
        ))
        onComplete.expects(zipFile)

        //when
        onAction(zipFile)

        //then
        findComponents(renderer.root, addToZipPopup()) should be (empty)
        inside(findComponentProps(renderer.root, statusPopupComp)) {
          case StatusPopupProps(text, title, closable, _) =>
            text shouldBe "Add item(s) to zip archive\n0%"
            title shouldBe "Status"
            closable shouldBe false
        }

        //when & then
        for {
          _ <- eventually(onNextItemFunc should not be null)
          _ = onNextItemFunc()
          _ <- eventually(
            findComponentProps(renderer.root, statusPopupComp).text shouldBe "Add item(s) to zip archive\n50%"
          )
          _ = onNextItemFunc()
          _ <- eventually(
            findComponentProps(renderer.root, statusPopupComp).text shouldBe "Add item(s) to zip archive\n100%"
          )
          _ = onNextItemFunc()
          _ <- eventually(
            findComponentProps(renderer.root, statusPopupComp).text shouldBe "Add item(s) to zip archive\n100%"
          )
          _ = p.success(())
          res <- eventually(
            findComponents(renderer.root, statusPopupComp()) should be (empty)
          )
        } yield res
    }
  }
}
