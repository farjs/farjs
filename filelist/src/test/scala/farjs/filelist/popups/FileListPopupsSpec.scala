package farjs.filelist.popups

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopups._
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.{FileListState, MockFileListActions}
import farjs.ui.popup._
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FileListPopupsSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  FileListPopups.messageBoxComp = mockUiComponent("MessageBox")
  FileListPopups.makeFolderPopupComp = mockUiComponent("MakeFolderPopup")
  FileListPopups.selectController = mockUiComponent("SelectController")
  FileListPopups.viewItemsPopupComp = mockUiComponent("ViewItemsPopup")
  FileListPopups.copyItemsComp = mockUiComponent("CopyItems")

  //noinspection TypeAnnotation
  class Actions {
    val deleteAction = mockFunction[Dispatch, String, Seq[FileListItem], FileListTaskAction]
    val createDir = mockFunction[Dispatch, String, String, Boolean, FileListDirCreateAction]

    val actions = new MockFileListActions(
      createDirMock = createDir,
      deleteActionMock = deleteAction
    )
  }

  it should "render initial component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState())
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    assertComponents(result.children, List(
      <(selectController())(^.assertWrapped(inside(_) {
        case SelectControllerProps(`dispatch`, actions.actions, `state`, props.popups) =>
      }))(),
      <(viewItemsPopupComp())(^.wrapped := props.popups)(),
      <(copyItemsComp())(^.wrapped := props.popups)()
    ))
  }
  
  "Help popup" should "dispatch FileListPopupHelpAction when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupHelpAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions.head.onAction()

    Succeeded
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, messageBoxComp) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Help"
        message shouldBe "//TODO: show help/about info"
        inside(resActions) {
          case List(MessageBoxAction("OK", _, true)) =>
        }
        style shouldBe Theme.current.popup.regular
    }
  }

  "Exit popup" should "dispatch FileListPopupExitAction and emit Ctrl+Q when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupExitAction(show = false)

    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    //then
    dispatch.expects(action)
    onKey.expects("e", true, false, false)

    //when
    msgBox.actions.head.onAction()

    //cleanup
    process.stdin.removeListener("keypress", listener)

    Succeeded
  }

  it should "dispatch FileListPopupExitAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupExitAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()

    Succeeded
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, messageBoxComp) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Exit"
        message shouldBe "Do you really want to exit FAR.js?"
        inside(resActions) {
          case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
        }
        style shouldBe Theme.current.popup.regular
    }
  }
  
  "Delete popup" should "call api and delete currItem when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = FileListState(currDir = currDir)
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
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
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
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
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListPopupDeleteAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()

    Succeeded
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, messageBoxComp) {
      case MessageBoxProps(title, message, resActions, style) =>
        title shouldBe "Delete"
        message shouldBe "Do you really want to delete selected item(s)?"
        inside(resActions) {
          case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
        }
        style shouldBe Theme.current.popup.error
    }
  }
  
  "MkFolder popup" should "call api and update state when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/sub-dir", isRoot = false, items = Seq.empty)
    val state = FileListState(isActive = true, currDir = currDir)
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMkFolderPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val renderer = createTestRenderer(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val popup = findComponentProps(renderer.root, makeFolderPopupComp)
    val action = FileListDirCreateAction(
      FutureTask("Creating...", Future.successful(()))
    )
    val dir = "test dir"
    val multiple = true

    //then
    actions.createDir.expects(dispatch, currDir.path, dir, multiple).returning(action)
    dispatch.expects(action)
    dispatch.expects(FileListPopupMkFolderAction(show = false))

    //when
    popup.onOk(dir, multiple)

    action.task.future.map { _ =>
      //then
      val updated = findComponentProps(renderer.root, makeFolderPopupComp)
      updated.folderName shouldBe dir
      updated.multiple shouldBe multiple
    }
  }

  it should "dispatch FileListPopupMkFolderAction when Cancel action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMkFolderPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val comp = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )
    val popup = findComponentProps(comp, makeFolderPopupComp)
    val action = FileListPopupMkFolderAction(show = false)

    //then
    dispatch.expects(action)

    //when
    popup.onCancel()

    Succeeded
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showMkFolderPopup = true))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = testRender(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, makeFolderPopupComp) {
      case MakeFolderPopupProps(folderName, multiple, _, _) =>
        folderName shouldBe ""
        multiple shouldBe false
    }
  }
}
