package farjs.filelist.copy

import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.copy.CopyProcess._
import farjs.ui.popup.MessageBoxProps
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.test._

class CopyProcessSpec extends TestSpec with TestRendererUtils {

  CopyProcess.copyProgressPopup = () => "CopyProgressPopup".asInstanceOf[ReactClass]
  CopyProcess.messageBoxComp = () => "MessageBox".asInstanceOf[ReactClass]
  
  it should "call onDone when YES action in cancel popup" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, onDone)
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()
    val cancelProps = findComponentProps(renderer.root, messageBoxComp)

    //then
    onDone.expects()
    
    //when
    cancelProps.actions.head.onAction()
  }

  it should "hide cancel popup when NO action" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, () => ())
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()
    val cancelProps = findComponentProps(renderer.root, messageBoxComp)
    
    //when
    cancelProps.actions.last.onAction()
    
    //then
    findProps(renderer.root, messageBoxComp) should be (empty)
  }

  it should "render cancel popup when onCancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, () => ())
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)

    //when
    progressProps.onCancel()

    //then
    inside(renderer.root.children.toList) { case List(_, cancel) =>
      assertTestComponent(cancel, messageBoxComp) {
        case MessageBoxProps(title, message, actions, style) =>
          title shouldBe "Operation has been interrupted"
          message shouldBe "Do you really want to cancel it?"
          inside(actions) { case List(yes, no) =>
            yes.label shouldBe "YES"
            no.label shouldBe "NO"
          }
          style shouldBe Theme.current.popup.error
      }
    }
  }

  it should "render CopyProgressPopup" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, () => ())

    //when
    val result = testRender(<(CopyProcess())(^.wrapped := props)())

    //then
    assertTestComponent(result, copyProgressPopup) {
      case CopyProgressPopupProps(item, to, itemPercent, resTotal, totalPercent, timeSeconds, leftSeconds, bytesPerSecond, _) =>
        item shouldBe "test.file"
        to shouldBe props.toPath
        itemPercent shouldBe 25
        resTotal shouldBe props.total
        totalPercent shouldBe 50
        timeSeconds shouldBe 5
        leftSeconds shouldBe 7
        bytesPerSecond shouldBe 345123
    }
  }
}
