package farclone.ui

import farclone.ui.TextBoxSpec._
import scommons.react.blessed._
import scommons.react.blessed.raw._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class TextBoxSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "move cursor and grab focus when onClick" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (programMock.omove _).expects(1, 2)
    (screenMock.focused _).expects().returning(null)
    (inputMock.focus _).expects()

    //when
    root.children(0).props.onClick(js.Dynamic.literal(x = 1, y = 2))
  }

  it should "keep cursor position when onResize" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (screenMock.focused _).expects().returning(inputMock.asInstanceOf[BlessedElement])
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(1, 2)

    //when
    root.children(0).props.onResize()
  }

  it should "show cursor when onFocus" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val cursorMock = mock[BlessedCursorMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.cursor _).expects().returning(cursorMock.asInstanceOf[BlessedCursor])
    (cursorMock.shape _).expects().returning("underline")
    (cursorMock.blink _).expects().returning(false)
    (screenMock.cursorShape _).expects("underline", true)
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (inputMock.aleft _).expects().returning(1)
    (inputMock.atop _).expects().returning(2)
    (programMock.omove _).expects(1, 2)
    (programMock.showCursor _).expects()

    //when
    root.children(0).props.onFocus()
  }

  it should "hide cursor when onBlur" in {
    //given
    val props = getTextBoxProps()
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val inputMock = mock[BlessedElementMock]
    val root = createTestRenderer(<(TextBox())(^.wrapped := props)(), { el =>
      if (el.`type` == "input".asInstanceOf[js.Any]) inputMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    (inputMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (programMock.hideCursor _).expects()

    //when
    root.children(0).props.onBlur()
  }

  it should "render component" in {
    //given
    val props = getTextBoxProps()

    //when
    val result = shallowRender(<(TextBox())(^.wrapped := props)())

    //then
    assertTextBox(result, props)
  }

  private def getTextBoxProps(value: String = "initial name",
                              onChange: String => Unit = _ => ()
                             ): TextBoxProps = TextBoxProps(
    pos = (1, 2),
    width = 10,
    value = value,
    style = new BlessedStyle {
      override val bg = "cyan"
      override val fg = "black"
    },
    onChange = onChange
  )

  private def assertTextBox(result: ShallowInstance, props: TextBoxProps): Unit = {
    val (left, top) = props.pos
    
    assertNativeComponent(result,
      <.input(
        ^.rbAutoFocus := false,
        ^.rbClickable := true,
        ^.rbKeyable := true,
        ^.rbWidth := props.width,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := props.value
      )()
    )
  }
}

object TextBoxSpec {

  @JSExportAll
  trait BlessedProgramMock {

    def showCursor(): Unit
    def hideCursor(): Unit

    def omove(x: Int, y: Int): Unit
  }

  @JSExportAll
  trait BlessedScreenMock {

    def program: BlessedProgram
    def cursor: BlessedCursor

    def focused: BlessedElement

    def cursorShape(shape: String, blink: Boolean): Boolean
  }

  @JSExportAll
  trait BlessedCursorMock {

    def shape: String
    def blink: Boolean
  }

  @JSExportAll
  trait BlessedElementMock {

    def aleft: Int
    def atop: Int

    def screen: BlessedScreen
    def focus(): Unit
  }
}
