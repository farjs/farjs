package farjs.viewer

import scommons.react.blessed._
import scommons.react.raw
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class ViewerInputSpec extends TestSpec with TestRendererUtils {

  it should "call onKeypress when onKeypress(...)" in {
    //given
    val onKeypress = mockFunction[String, Unit]
    val onMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val offMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val input = literal(
      "on" -> onMock,
      "off" -> offMock
    ).asInstanceOf[BlessedElement]

    val inputRef = raw.React.createRef()
    inputRef.current = input
    val props = ViewerInputProps(inputRef, onKeypress = onKeypress)

    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null
    onMock.expects("keypress", *).onCall { (_: String, listener: js.Function) =>
      keyListener = listener.asInstanceOf[js.Function2[js.Object, KeyboardKey, Unit]]
      input
    }
    onMock.expects("wheelup", *)
    onMock.expects("wheeldown", *)
    val renderer = createTestRenderer(<(ViewerInput())(^.plain := props)())
    val keyFull = "some-key"

    //then
    onKeypress.expects(keyFull)

    //when
    keyListener(null, literal(full = keyFull).asInstanceOf[KeyboardKey])

    //cleanup
    offMock.expects("keypress", keyListener)
    offMock.expects("wheelup", *)
    offMock.expects("wheeldown", *)
    renderer.unmount()
  }

  it should "call onWheel when onWheelup/onWheeldown" in {
    //given
    val onWheel = mockFunction[Boolean, Unit]
    val onMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val offMock = mockFunction[String, js.Function, BlessedEventEmitter]
    val input = literal(
      "on" -> onMock,
      "off" -> offMock
    ).asInstanceOf[BlessedElement]

    val inputRef = raw.React.createRef()
    inputRef.current = input
    val props = ViewerInputProps(inputRef, onWheel = onWheel)

    var wheelupListener: js.Function1[MouseData, Unit] = null
    var wheeldownListener: js.Function1[MouseData, Unit] = null
    onMock.expects("wheelup", *).onCall { (_: String, listener: js.Function) =>
      wheelupListener = listener.asInstanceOf[js.Function1[MouseData, Unit]]
      input
    }
    onMock.expects("wheeldown", *).onCall { (_: String, listener: js.Function) =>
      wheeldownListener = listener.asInstanceOf[js.Function1[MouseData, Unit]]
      input
    }
    onMock.expects("keypress", *)

    val renderer = createTestRenderer(<(ViewerInput())(^.plain := props)())

    def check(up: Boolean): Unit = {
      //then
      onWheel.expects(up)

      //when
      if (up) wheelupListener(literal().asInstanceOf[MouseData])
      else wheeldownListener(literal().asInstanceOf[MouseData])
    }

    //when & then
    check(up = false)
    check(up = true)

    //cleanup
    offMock.expects("keypress", *)
    offMock.expects("wheelup", wheelupListener)
    offMock.expects("wheeldown", wheeldownListener)
    renderer.unmount()
  }

  it should "render children" in {
    //given
    val inputRef = raw.React.createRef()
    val props = ViewerInputProps(inputRef)

    //when
    val result = createTestRenderer(<(ViewerInput())(^.plain := props)(
      <.text()("test_child")
    )).root

    //then
    assertComponents(result.children, List(
      <.text()("test_child")
    ))
  }
}
