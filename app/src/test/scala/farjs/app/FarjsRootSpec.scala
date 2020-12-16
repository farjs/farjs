package farjs.app

import farjs.app.FarjsRootSpec._
import farjs.ui.LogPanel
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FarjsRootSpec extends TestSpec
  with TestRendererUtils
  with ShallowRendererUtils {

  private val fileListComp = new FunctionComponent[Unit] {
    protected def render(props: Props): ReactElement = {
      <.>()("FileList Comp")
    }
  }.apply()

  private val fileListPopups = new FunctionComponent[Unit] {
    protected def render(props: Props): ReactElement = {
      <.>()("FileListPopups Comp")
    }
  }.apply()

  private val taskController = new FunctionComponent[Unit] {
    protected def render(props: Props): ReactElement = {
      <.>()("TaskController Comp")
    }
  }.apply()

  it should "emit resize event when on F12" in {
    //given
    val root = new FarjsRoot(fileListComp, fileListPopups, taskController, showDevTools = false)
    val programMock = mock[BlessedProgramMock]
    val screenMock = mock[BlessedScreenMock]
    val boxMock = mock[BlessedElementMock]
    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null

    (boxMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.program _).expects().returning(programMock.asInstanceOf[BlessedProgram])
    (screenMock.key _).expects(*, *).onCall { (keys, listener) =>
      keys.toList shouldBe List("f12")
      keyListener = listener
    }
    (programMock.emit _).expects("resize")
    
    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    })
    findComponents(renderer.root.children(0), "box").head.props.width shouldBe "100%"
    
    //when
    TestRenderer.act { () =>
      keyListener(null, null)
    }

    //then
    findComponents(renderer.root.children(0), "box").head.props.width shouldBe "70%"
  }

  it should "render component without DevTools" in {
    //given
    val root = new FarjsRoot(fileListComp, fileListPopups, taskController, showDevTools = false)

    //when
    val result = shallowRender(<(root())()())

    //then
    assertNativeComponent(result,
      <.>()(
        <.box(
          ^.rbWidth := "100%"
        )(
          <(WithPortals())()(
            <.>()(
              Portal.create(
                <(fileListComp).empty
              ),
              <(fileListPopups).empty,
              <(taskController).empty
            )
          )
        )
      )
    )
  }
  
  it should "render component with DevTools" in {
    //given
    val root = new FarjsRoot(fileListComp, fileListPopups, taskController, showDevTools = true)

    //when
    val result = shallowRender(<(root())()())

    //then
    assertNativeComponent(result,
      <.>()(
        <.box(
          ^.rbWidth := "70%"
        )(
          <(WithPortals())()(
            <.>()(
              Portal.create(
                <(fileListComp).empty
              ),
              <(fileListPopups).empty,
              <(taskController).empty
            )
          )
        ),
        <.box(
          ^.rbWidth := "30%",
          ^.rbHeight := "100%",
          ^.rbLeft := "70%"
        )(
          <(LogPanel())()()
          //<(ColorPanel())()()
        )
      )
    )
  }
}

object FarjsRootSpec {

  @JSExportAll
  trait BlessedProgramMock {

    def emit(eventName: String): Unit
  }

  @JSExportAll
  trait BlessedScreenMock {

    def program: BlessedProgram

    def key(keys: js.Array[String], onKey: js.Function2[js.Object, KeyboardKey, Unit]): Unit
  }

  @JSExportAll
  trait BlessedElementMock {

    def screen: BlessedScreen
  }
}
