package farclone.ui.filelist.popups

import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.popup._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.ShallowRendererUtils

import scala.scalajs.js

class FileListPopupsSpec extends TestSpec with ShallowRendererUtils {

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState())

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()())
  }
  
  "Help popup" should "dispatch FileListHelpAction when OK action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListHelpAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions.head.onAction()
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showHelpPopup = true))

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(helpPopup) =>
      assertComponent(helpPopup, MessageBox) {
        case MessageBoxProps(title, message, actions, style) =>
          title shouldBe "Help"
          message shouldBe "//TODO: show help/about info"
          inside(actions) {
            case List(MessageBoxAction("OK", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
  }

  "Exit popup" should "dispatch FileListExitAction and emit Ctrl+C when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListExitAction(show = false)

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
  }

  it should "dispatch FileListExitAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListExitAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showExitPopup = true))

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(helpPopup) =>
      assertComponent(helpPopup, MessageBox) {
        case MessageBoxProps(title, message, actions, style) =>
          title shouldBe "Exit"
          message shouldBe "Do you really want to exit FARc?"
          inside(actions) {
            case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
  }
  
  behavior of "Delete popup" 
  
  ignore should "dispatch FileListDeleteAction and call api when YES action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListDeleteAction(show = false)

    //then
    dispatch.expects(action)
    //TODO: check api call

    //when
    msgBox.actions.head.onAction()
  }

  it should "dispatch FileListDeleteAction when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))
    val comp = shallowRender(<(FileListPopups())(^.wrapped := props)())
    val msgBox = findComponentProps(comp, MessageBox)
    val action = FileListDeleteAction(show = false)

    //then
    dispatch.expects(action)

    //when
    msgBox.actions(1).onAction()
  }

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val props = FileListPopupsProps(dispatch, FileListPopupsState(showDeletePopup = true))

    //when
    val result = shallowRender(<(FileListPopups())(^.wrapped := props)())

    //then
    assertNativeComponent(result, <.>()(), { case List(helpPopup) =>
      assertComponent(helpPopup, MessageBox) {
        case MessageBoxProps(title, message, actions, style) =>
          title shouldBe "Delete"
          message shouldBe "Do you really want to delete selected item(s)?"
          inside(actions) {
            case List(MessageBoxAction("YES", _, false), MessageBoxAction("NO", _, true)) =>
          }
          style shouldBe Popup.Styles.normal
      }
    })
  }
}
