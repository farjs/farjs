package farjs.filelist.popups

import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.DeleteController._
import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class DeleteControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  DeleteController.messageBoxComp = mockUiComponent("MessageBox")

  //noinspection TypeAnnotation
  class Actions {
    val deleteAction = mockFunction[Dispatch, String, Seq[FileListItem], FileListTaskAction]

    val actions = new MockFileListActions(
      deleteActionMock = deleteAction
    )
  }

  it should "call api and delete currItem when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = FileListState(currDir = currDir)
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showDeletePopup = true))
    val comp = testRender(<(DeleteController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp, plain = true)
    val deleteAction = FileListTaskAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 1"))

    //then
    actions.deleteAction.expects(dispatch, currDir.path, items).returning(deleteAction)
    dispatch.expects(deleteAction)
    dispatch.expects(FileListPopupDeleteAction(show = false))

    //when
    msgBox.actions.head.onAction()

    deleteAction.task.future.map(_ => Succeeded)
  }

  it should "call api and delete selectedItems when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = FileListState(isActive = true, currDir = currDir, selectedNames = Set("file 2"))
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showDeletePopup = true))
    val comp = testRender(<(DeleteController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp, plain = true)
    val deleteAction = FileListTaskAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 2"))

    //then
    actions.deleteAction.expects(dispatch, currDir.path, items).returning(deleteAction)
    dispatch.expects(deleteAction)
    dispatch.expects(FileListPopupDeleteAction(show = false))

    //when
    msgBox.actions.head.onAction()

    deleteAction.task.future.map(_ => Succeeded)
  }

  it should "dispatch FileListPopupDeleteAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showDeletePopup = true))
    val comp = testRender(<(DeleteController())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp, plain = true)
    val action = FileListPopupDeleteAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = PopupControllerProps(Some(FileListData(dispatch, actions.actions, state)),
      FileListPopupsState(showDeletePopup = true))

    //when
    val result = testRender(<(DeleteController())(^.wrapped := props)())

    //then
    assertTestComponent(result, messageBoxComp, plain = true) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Delete"
        message shouldBe "Do you really want to delete selected item(s)?"
        inside(resActions.toList) {
          case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
        }
        style shouldBe Theme.current.popup.error
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
    val renderer = createTestRenderer(<(DeleteController())(^.wrapped := props)())

    //then
    renderer.root.children.toList should be (empty)
  }
}
