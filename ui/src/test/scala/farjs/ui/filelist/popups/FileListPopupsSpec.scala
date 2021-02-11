package farjs.ui.filelist.popups

import farjs.api.filelist.{FileListDir, FileListItem}
import farjs.ui.filelist.FileListActions._
import farjs.ui.filelist.popups.FileListPopups._
import farjs.ui.filelist.popups.FileListPopupsActions._
import farjs.ui.filelist.{FileListActions, FileListsState}
import farjs.ui.popup._
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FileListPopupsSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  FileListPopups.messageBoxComp = () => "MessageBox".asInstanceOf[ReactClass]
  FileListPopups.makeFolderPopupComp = () => "MakeFolderPopup".asInstanceOf[ReactClass]

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState())

    //when
    val result = createTestRenderer(<(FileListPopups())(^.wrapped := props)())

    //then
    result.root.children.toList should be (empty)
  }
  
  "Help popup" should "dispatch FileListPopupHelpAction when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showHelpPopup = true)
    ))
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showHelpPopup = true)
    ))

    //when
    val result = testRender(<(FileListPopups())(^.wrapped := props)())

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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showExitPopup = true)
    ))
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
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
    onKey.expects("q", true, false, false)

    //when
    msgBox.actions.head.onAction()

    //cleanup
    process.stdin.removeListener("keypress", listener)

    Succeeded
  }

  it should "dispatch FileListPopupExitAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showExitPopup = true)
    ))
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showExitPopup = true)
    ))

    //when
    val result = testRender(<(FileListPopups())(^.wrapped := props)())

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
    val actions = mock[FileListActions]
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(currDir = currDir),
        popups = FileListPopupsState(showDeletePopup = true)
      )
    }
    val props = FileListPopupsProps(dispatch, actions, state)
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListItemsDeleteAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 1"))

    //then
    (actions.deleteItems _).expects(dispatch, false, currDir.path, items).returning(action)
    dispatch.expects(action)
    dispatch.expects(FileListPopupDeleteAction(show = false))

    //when
    msgBox.actions.head.onAction()

    action.task.future.map(_ => Succeeded)
  }

  it should "call api and delete selectedItems when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = false),
        right = state.right.copy(isActive = true, currDir = currDir, selectedNames = Set("file 2")),
        popups = FileListPopupsState(showDeletePopup = true)
      )
    }
    val props = FileListPopupsProps(dispatch, actions, state)
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, messageBoxComp)
    val action = FileListItemsDeleteAction(
      FutureTask("Deleting Items", Future.successful(()))
    )
    val items = List(FileListItem("file 2"))

    //then
    (actions.deleteItems _).expects(dispatch, true, currDir.path, items).returning(action)
    dispatch.expects(action)
    dispatch.expects(FileListPopupDeleteAction(show = false))

    //when
    msgBox.actions.head.onAction()

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch FileListPopupDeleteAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showDeletePopup = true)
    ))
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showDeletePopup = true)
    ))

    //when
    val result = testRender(<(FileListPopups())(^.wrapped := props)())

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
    val actions = mock[FileListActions]
    val currDir = FileListDir("/sub-dir", isRoot = false, items = Seq.empty)
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(isActive = true, currDir = currDir),
        popups = FileListPopupsState(showMkFolderPopup = true)
      )
    }
    val props = FileListPopupsProps(dispatch, actions, state)
    val renderer = createTestRenderer(<(FileListPopups())(^.wrapped := props)())
    val popup = findComponentProps(renderer.root, makeFolderPopupComp)
    val action = FileListDirCreateAction(
      FutureTask("Creating...", Future.successful(()))
    )
    val dir = "test dir"
    val multiple = true

    //then
    (actions.createDir _).expects(dispatch, false, currDir.path, dir, multiple).returning(action)
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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showMkFolderPopup = true)
    ))
    val comp = testRender(<(FileListPopups())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showMkFolderPopup = true)
    ))

    //when
    val result = testRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertTestComponent(result, makeFolderPopupComp) {
      case MakeFolderPopupProps(folderName, multiple, _, _) =>
        folderName shouldBe ""
        multiple shouldBe false
    }
  }
}
