package farjs.filelist.copy

import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.copy.CopyProcess._
import farjs.filelist.copy.CopyProcessSpec._
import farjs.ui.popup.MessageBoxProps
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.nodejs.raw.Timers
import scommons.react._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class CopyProcessSpec extends TestSpec with TestRendererUtils {

  CopyProcess.copyProgressPopup = () => "CopyProgressPopup".asInstanceOf[ReactClass]
  CopyProcess.messageBoxComp = () => "MessageBox".asInstanceOf[ReactClass]
  
  CopyProcess.timers = new TimersMock {

    def setInterval(callback: js.Function0[Any], delay: Double): Timeout = {
      js.Dynamic.literal().asInstanceOf[Timeout]
    }

    def clearInterval(timeout: Timeout): Unit = {
    }
  }.asInstanceOf[Timers]
  
  it should "increment timeSeconds every second" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val timers = mock[TimersMock]
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, () => ())
    val timerId = js.Dynamic.literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    (timers.setInterval _).expects(*, 1000).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    
    //when & then
    onTimer()
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 1
    
    //when & then
    onTimer()
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 2
    
    //then
    (timers.clearInterval _).expects(timerId)
    
    //when
    TestRenderer.act { () =>
      renderer.unmount()
    }
    
    //cleanup
    CopyProcess.timers = savedTimers
  }

  it should "not increment timeSeconds when cancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val timers = mock[TimersMock]
    val savedTimers = CopyProcess.timers
    CopyProcess.timers = timers.asInstanceOf[Timers]
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
      FileListItem("dir 1", isDir = true)
    ), "/to/path", 12345, () => ())
    val timerId = js.Dynamic.literal().asInstanceOf[Timeout]

    //then
    var onTimer: js.Function0[Any] = null
    (timers.setInterval _).expects(*, 1000).onCall { (callback: js.Function0[Any], _) =>
      onTimer = callback
      timerId
    }
    val renderer = createTestRenderer(<(CopyProcess())(^.wrapped := props)())
    val progressProps = findComponentProps(renderer.root, copyProgressPopup)
    progressProps.onCancel()

    //when & then
    onTimer()
    findComponentProps(renderer.root, copyProgressPopup).timeSeconds shouldBe 0
    
    //cleanup
    (timers.clearInterval _).expects(timerId)
    TestRenderer.act { () =>
      renderer.unmount()
    }
    CopyProcess.timers = savedTimers
  }

  it should "call onDone when YES action in cancel popup" in {
    //given
    val onDone = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
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
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
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
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
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
    val props = CopyProcessProps(dispatch, actions, "/from/path", List(
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
        timeSeconds shouldBe 0
        leftSeconds shouldBe 7
        bytesPerSecond shouldBe 345123
    }
  }
}

object CopyProcessSpec {

  @JSExportAll
  trait TimersMock {

    def setInterval(callback: js.Function0[Any], delay: Double): Timeout

    def clearInterval(timeout: Timeout): Unit
  }
}
