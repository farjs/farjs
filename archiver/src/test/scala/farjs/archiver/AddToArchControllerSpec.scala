package farjs.archiver

import farjs.archiver.AddToArchController._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.popup.StatusPopupProps
import farjs.ui.task.Task
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

class AddToArchControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  AddToArchController.addToArchPopup = mockUiComponent("AddToArchPopup")
  AddToArchController.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]

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
    val props = AddToArchControllerProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2")
      ) ++ items),
      selectedNames = Set("dir 3")
    ), zipName = "new.zip", items, AddToArchAction.Add, onComplete, onCancel)
    val addToArchApi = mockFunction[String, String, Set[String], () => Unit, Future[Unit]]
    AddToArchController.addToArchApi = addToArchApi

    //when
    val renderer = createTestRenderer(<(AddToArchController())(^.wrapped := props)())

    //then
    findComponents(renderer.root, statusPopupComp) should be (empty)
    inside(findComponentProps(renderer.root, addToArchPopup)) {
      case AddToArchPopupProps(zipName, action, onAction, onCancel) =>
        zipName shouldBe "new.zip"
        action shouldBe AddToArchAction.Add
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
        val error = new Exception("test error")
        val addToZipF = Future.failed(error)
        addToArchApi.expects(zipFile, props.state.currDir.path, Set("dir 3"), *).returning(addToZipF)
        onComplete.expects(*).never()
        var resultF: Future[_] = null
        dispatch.expects(*).onCall { action: Any =>
          inside(action.asInstanceOf[FileListTaskAction]) {
            case FileListTaskAction(Task("Add item(s) to zip archive", future)) =>
              resultF = future
          }
        }

        //when
        onAction(zipFile)

        //then
        findComponents(renderer.root, addToArchPopup()) should be (empty)
        p.success(true)
        eventually {
          findComponents(renderer.root, statusPopupComp) should be (empty)
        }.flatMap { _ =>
          resultF.failed.map(_ shouldBe error)
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
    val props = AddToArchControllerProps(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2")
      ) ++ items),
      selectedNames = Set("dir 3")
    ), zipName = "new.zip", items, AddToArchAction.Add, onComplete, onCancel)
    val addToArchApi = mockFunction[String, String, Set[String], () => Unit, Future[Unit]]
    AddToArchController.addToArchApi = addToArchApi

    //when
    val renderer = createTestRenderer(<(AddToArchController())(^.wrapped := props)())

    //then
    findComponents(renderer.root, statusPopupComp) should be (empty)
    inside(findComponentProps(renderer.root, addToArchPopup)) {
      case AddToArchPopupProps(zipName, action, onAction, onCancel) =>
        zipName shouldBe "new.zip"
        action shouldBe AddToArchAction.Add
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
        addToArchApi.expects(zipFile, props.state.currDir.path, Set("dir 3"), *).onCall { (_, _, _, onNextItem) =>
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
        findComponents(renderer.root, addToArchPopup()) should be (empty)
        val statusPopup = inside(findComponents(renderer.root, statusPopupComp)) {
          case List(p) => p
        }
        assertNativeComponent(statusPopup, <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
          case StatusPopupProps(text, title, onClose) =>
            text shouldBe "Add item(s) to zip archive\n0%"
            title shouldBe js.undefined
            onClose shouldBe js.undefined
        }))())

        //when & then
        for {
          _ <- eventually(onNextItemFunc should not be null)
          _ = onNextItemFunc()
          _ <- eventually(
            inside(findComponents(renderer.root, statusPopupComp)) {
              case List(p) => p.props.asInstanceOf[StatusPopupProps].text shouldBe "Add item(s) to zip archive\n50%"
            }
          )
          _ = onNextItemFunc()
          _ <- eventually(
            inside(findComponents(renderer.root, statusPopupComp)) {
              case List(p) => p.props.asInstanceOf[StatusPopupProps].text shouldBe "Add item(s) to zip archive\n100%"
            }
          )
          _ = onNextItemFunc()
          _ <- eventually(
            inside(findComponents(renderer.root, statusPopupComp)) {
              case List(p) => p.props.asInstanceOf[StatusPopupProps].text shouldBe "Add item(s) to zip archive\n100%"
            }
          )
          _ = p.success(())
          res <- eventually(
            findComponents(renderer.root, statusPopupComp) should be (empty)
          )
        } yield res
    }
  }
}
