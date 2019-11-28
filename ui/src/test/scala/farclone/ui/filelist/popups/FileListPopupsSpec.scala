package farclone.ui.filelist.popups

import farclone.api.filelist.{FileListDir, FileListItem}
import farclone.ui.filelist.FileListActions.FileListItemsDeleteAction
import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.filelist.{FileListActions, FileListsState}
import farclone.ui.popup._
import org.scalatest.Succeeded
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test.BaseTestSpec
import scommons.react.test.util.ShallowRendererUtils

import scala.concurrent.Future
import scala.scalajs.js

class FileListPopupsSpec extends AsyncTestSpec with BaseTestSpec
  with ShallowRendererUtils {

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState())

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()())
    
    Succeeded
  }
  
  "Help popup" should "dispatch FileListPopupHelpAction when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showHelpPopup = true)
    ))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(popup) =>
      assertComponent(popup, MessageBox) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Help"
          message shouldBe "//TODO: show help/about info"
          inside(resActions) {
            case List(MessageBoxAction("OK", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
  }

  "Exit popup" should "dispatch FileListPopupExitAction and emit Ctrl+C when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showExitPopup = true)
    ))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    onKey.expects("c", true, false, false)

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
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(popup) =>
      assertComponent(popup, MessageBox) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Exit"
          message shouldBe "Do you really want to exit FARc?"
          inside(resActions) {
            case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
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
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
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
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(popup) =>
      assertComponent(popup, MessageBox) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Delete"
          message shouldBe "Do you really want to delete selected item(s)?"
          inside(resActions) {
            case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
          }
          style shouldBe Popup.Styles.error
      }
    })
  }
  
  behavior of "MkFolder popup"
  
  ignore should "call api and create single folder when OK action" in {
    //TODO: add test case
    Succeeded
  }

  ignore should "call api and create multiple folders when OK action" in {
    //TODO: add test case
    Succeeded
  }

  it should "dispatch FileListPopupMkFolderAction when Cancel action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showMkFolderPopup = true)
    ))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val popup = findComponentProps(comp, MakeFolderPopup)
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
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(popup) =>
      assertComponent(popup, MakeFolderPopup) {
        case MakeFolderPopupProps(folderName, multiple, _, _) =>
          folderName shouldBe ""
          multiple shouldBe false
      }
    })
  }
}
