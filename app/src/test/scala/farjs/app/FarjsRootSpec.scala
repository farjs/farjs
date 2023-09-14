package farjs.app

import farjs.app.FarjsRoot._
import farjs.ui.task.TaskManagerProps
import farjs.ui.theme.{DefaultTheme, Theme, XTerm256Theme}
import farjs.ui.tool._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FarjsRootSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  private val mainComp = mockUiComponent[Unit]("AppMainUIMock").apply()
  
  FarjsRoot.taskControllerComp = mockUiComponent("TaskManager")
  FarjsRoot.logControllerComp = "LogController".asInstanceOf[ReactClass]
  FarjsRoot.devToolPanelComp = "DevToolPanel".asInstanceOf[ReactClass]
  
  private val mainUiF: js.Function1[Any, Unit] => Future[(Theme, ReactClass)] = { _ =>
    Future.successful((XTerm256Theme, mainComp))
  }

  it should "set devTool and emit resize event when on F12" in {
    //given
    val root = new FarjsRoot(mainUiF, DevTool.Hidden, DefaultTheme)
    val emitMock = mockFunction[String, Unit]
    val program = literal("emit" -> emitMock)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val offMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("program" -> program, "on" -> onMock, "off" -> offMock)
    val boxMock = literal("screen" -> screen)
    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null

    onMock.expects("keypress", *).onCall { (_, listener) =>
      keyListener = listener
    }
    var emitCalled = false
    emitMock.expects("resize").onCall { _: String =>
      emitCalled = true
    }
    
    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    })
    findComponents(renderer.root.children(0), <.box.name).head.props.width shouldBe "100%"
    
    //when
    TestRenderer.act { () =>
      keyListener(null, literal(full = "f12").asInstanceOf[KeyboardKey])
    }

    //then
    findComponents(renderer.root.children(0), <.box.name).head.props.width shouldBe "70%"
    
    eventually {
      emitCalled shouldBe true
    }.map { _ =>
      //then
      offMock.expects("keypress", keyListener)

      //when
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "set devTool when onActivate" in {
    //given
    val root = new FarjsRoot(mainUiF, DevTool.Colors, DefaultTheme)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("on" -> onMock)
    val boxMock = literal("screen" -> screen)
    onMock.expects("keypress", *)

    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    })
    val devToolProps = {
      val logProps = inside(findComponents(renderer.root, logControllerComp)) {
        case List(log) => log.props.asInstanceOf[LogControllerProps]
      }
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      inside(findComponents(renderedContent, devToolPanelComp)) {
        case List(devToolPanel) => devToolPanel.props.asInstanceOf[DevToolPanelProps]
      }
    }
    devToolProps.devTool shouldBe DevTool.Colors

    //when
    devToolProps.onActivate(DevTool.Logs)
    
    //then
    val updatedProps = {
      val logProps = inside(findComponents(renderer.root, logControllerComp)) {
        case List(log) => log.props.asInstanceOf[LogControllerProps]
      }
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      inside(findComponents(renderedContent, devToolPanelComp)) {
        case List(devToolPanel) => devToolPanel.props.asInstanceOf[DevToolPanelProps]
      }
    }
    updatedProps.devTool shouldBe DevTool.Logs
  }

  it should "render mainUi when onReady" in {
    //given
    def getThemeCtxHook: (AtomicReference[Theme], ReactClass) = {
      val ref = new AtomicReference[Theme](null)
      (ref, new FunctionComponent[Unit] {
        protected def render(props: Props): ReactElement = {
          val ctx = Theme.useTheme()
          ref.set(ctx)
          props.children
        }
      }.apply())
    }
    val (themeCtx, mainComp) = getThemeCtxHook
    val mainUiF: js.Function1[Any, Unit] => Future[(Theme, ReactClass)] = { _ =>
      Future.successful((XTerm256Theme, mainComp))
    }
    val root = new FarjsRoot(mainUiF, DevTool.Hidden, DefaultTheme)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("on" -> onMock)
    val boxMock = literal("screen" -> screen)
    onMock.expects("keypress", *)

    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    })

    assertComponents(renderer.root.children, List(
      <.box(^.rbWidth := "100%")(
        <.text()("Loading...")
      ),
      <(logControllerComp)(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(onReady, render) =>
          render("test log content") shouldBe null

          //when
          onReady()
      }))()
    ))

    //then
    eventually {
      themeCtx.get() shouldBe XTerm256Theme
      assertComponents(renderer.root.children, List(
        <.box(^.rbWidth := "100%")(
          <(mainComp)()(
            <(taskControllerComp())(^.assertPlain[TaskManagerProps](inside(_) {
              case TaskManagerProps(currentTask) => currentTask shouldBe js.undefined
            }))()
          )
        ),
        <(logControllerComp).empty
      ))
    }
  }
  
  it should "render component without DevTools" in {
    //given
    val root = new FarjsRoot(mainUiF, DevTool.Hidden, DefaultTheme)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("on" -> onMock)
    val boxMock = literal("screen" -> screen)
    onMock.expects("keypress", *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    }).root

    //then
    assertComponents(result.children, List(
      <.box(^.rbWidth := "100%")(
        <.text()("Loading...")
      ),
      <(logControllerComp)(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(_, render) =>
          render("test log content") shouldBe null
      }))()
    ))
  }
  
  it should "render component with DevTools" in {
    //given
    val root = new FarjsRoot(mainUiF, DevTool.Logs, DefaultTheme)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("on" -> onMock)
    val boxMock = literal("screen" -> screen)
    onMock.expects("keypress", *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    }).root

    //then
    assertComponents(result.children, List(
      <.box(^.rbWidth := "70%")(
        <.text()("Loading...")
      ),
      <(logControllerComp)(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(_, render) =>
          val content = "test log content"

          assertNativeComponent(createTestRenderer(render(content)).root,
            <.box(
              ^.rbWidth := "30%",
              ^.rbHeight := "100%",
              ^.rbLeft := "70%"
            )(
              <(devToolPanelComp)(^.assertPlain[DevToolPanelProps](inside(_) {
                case DevToolPanelProps(devTool, logContent, _) =>
                  devTool shouldBe DevTool.Logs
                  logContent shouldBe content
              }))()
            )
          )
      }))()
    ))
  }
}
