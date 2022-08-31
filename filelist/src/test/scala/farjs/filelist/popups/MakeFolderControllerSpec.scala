package farjs.filelist.popups

import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.FileListDir
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.MakeFolderController._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class MakeFolderControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  MakeFolderController.makeFolderPopup = mockUiComponent("MakeFolderPopup")

  //noinspection TypeAnnotation
  class Actions {
    val createDir = mockFunction[Dispatch, String, String, Boolean, FileListDirCreateAction]

    val actions = new MockFileListActions(
      createDirMock = createDir
    )
  }

  it should "call api and update state when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = Seq.empty)
    val state = FileListState(isActive = true, currDir = currDir)
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))
    val renderer = createTestRenderer(<(MakeFolderController())(^.wrapped := props)())
    val action = FileListDirCreateAction(
      FutureTask("Creating...", Future.successful(()))
    )
    val dir1 = "test dir"
    val dir2 = "test dir 2"
    val multiple = true

    //then
    actions.createDir.expects(dispatch, currDir.path, dir1, multiple).returning(action).twice()
    actions.createDir.expects(dispatch, currDir.path, dir2, multiple).returning(action)
    dispatch.expects(action).repeated(3)
    dispatch.expects(FileListPopupMkFolderAction(show = false)).repeated(3)

    for {
      _ <- Future.unit

      //when & then
      _ = findComponentProps(renderer.root, makeFolderPopup).onOk(dir1, multiple)
      _ <- action.task.future.map { _ =>
        inside(findComponentProps(renderer.root, makeFolderPopup)) {
          case MakeFolderPopupProps(folderItems, folderName, multiple, _, _) =>
            folderItems shouldBe List(dir1)
            folderName shouldBe dir1
            multiple shouldBe multiple
        }
      }

      //when & then
      _ = findComponentProps(renderer.root, makeFolderPopup).onOk(dir2, multiple)
      _ <- action.task.future.map { _ =>
        inside(findComponentProps(renderer.root, makeFolderPopup)) {
          case MakeFolderPopupProps(folderItems, folderName, multiple, _, _) =>
            folderItems shouldBe List(dir2, dir1)
            folderName shouldBe dir2
            multiple shouldBe multiple
        }
      }

      //when & then
      _ = findComponentProps(renderer.root, makeFolderPopup).onOk(dir1, multiple)
      _ <- action.task.future.map { _ =>
        inside(findComponentProps(renderer.root, makeFolderPopup)) {
          case MakeFolderPopupProps(folderItems, folderName, multiple, _, _) =>
            folderItems shouldBe List(dir1, dir2)
            folderName shouldBe dir1
            multiple shouldBe multiple
        }
      }
    } yield Succeeded
  }

  it should "dispatch FileListPopupMkFolderAction when Cancel action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))
    val comp = testRender(<(MakeFolderController())(^.wrapped := props)())
    val popup = findComponentProps(comp, makeFolderPopup)

    //then
    dispatch.expects(FileListPopupMkFolderAction(show = false))

    //when
    popup.onCancel()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showMkFolderPopup = true))

    //when
    val result = testRender(<(MakeFolderController())(^.wrapped := props)())

    //then
    assertTestComponent(result, makeFolderPopup) {
      case MakeFolderPopupProps(folderItems, folderName, multiple, _, _) =>
        folderItems shouldBe Nil
        folderName shouldBe ""
        multiple shouldBe false
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions, state)),
      FileListPopupsState())

    //when
    val renderer = createTestRenderer(<(MakeFolderController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
