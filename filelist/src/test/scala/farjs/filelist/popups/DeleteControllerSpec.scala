package farjs.filelist.popups

import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.DeleteController._
import farjs.ui.Dispatch
import farjs.ui.popup._
import farjs.ui.task.FutureTask
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.concurrent.Future

class DeleteControllerSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  DeleteController.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]

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
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showDeletePopup = true,
      data = Some(FileListData(dispatch, actions.actions, state)),
      onClose = onClose
    )
    val comp = testRender(withThemeContext(<(DeleteController())(^.wrapped := props)()))
    val msgBox = inside(findComponents(comp, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }
    val deleteAction = FileListTaskAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 1"))

    //then
    actions.deleteAction.expects(dispatch, currDir.path, items).returning(deleteAction)
    dispatch.expects(deleteAction)
    onClose.expects()

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
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showDeletePopup = true,
      data = Some(FileListData(dispatch, actions.actions, state)),
      onClose = onClose
    )
    val comp = testRender(withThemeContext(<(DeleteController())(^.wrapped := props)()))
    val msgBox = inside(findComponents(comp, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }
    val deleteAction = FileListTaskAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 2"))

    //then
    actions.deleteAction.expects(dispatch, currDir.path, items).returning(deleteAction)
    dispatch.expects(deleteAction)
    onClose.expects()

    //when
    msgBox.actions.head.onAction()

    deleteAction.task.future.map(_ => Succeeded)
  }

  it should "call onClose when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListUiData(
      showDeletePopup = true,
      data = Some(FileListData(dispatch, actions.actions, state)),
      onClose = onClose
    )
    val comp = testRender(withThemeContext(<(DeleteController())(^.wrapped := props)()))
    val msgBox = inside(findComponents(comp, messageBoxComp)) {
      case List(msgBox) => msgBox.props.asInstanceOf[MessageBoxProps]
    }

    //then
    onClose.expects()

    //when
    msgBox.actions(1).onAction()

    Succeeded
  }

  it should "render popup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListUiData(
      showDeletePopup = true,
      data = Some(FileListData(dispatch, actions.actions, state))
    )

    //when
    val result = testRender(withThemeContext(<(DeleteController())(^.wrapped := props)()))

    //then
    val currTheme = DefaultTheme
    assertNativeComponent(result, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Delete"
        message shouldBe "Do you really want to delete selected item(s)?"
        inside(resActions.toList) {
          case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
        }
        style shouldBe currTheme.popup.error
    }))())
  }

  it should "render empty component if showDeletePopup=false" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListUiData(data = Some(FileListData(dispatch, actions, state)))

    //when
    val renderer = createTestRenderer(withThemeContext(<(DeleteController())(^.wrapped := props)()))

    //then
    renderer.root.children.toList should be (empty)
  }

  it should "render empty component if data is None" in {
    //given
    val props = FileListUiData(showDeletePopup = true)

    //when
    val renderer = createTestRenderer(withThemeContext(<(DeleteController())(^.wrapped := props)()))

    //then
    renderer.root.children.toList should be (empty)
  }
}
