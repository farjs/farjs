package farjs.app

import farjs.app.FarjsRoot._
import farjs.ui.task.TaskManagerProps
import farjs.ui.tool._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FarjsRootSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  private val fileListComp = mockUiComponent[Unit]("FileListBrowser").apply()
  
  FarjsRoot.taskControllerComp = mockUiComponent("TaskManager")
  FarjsRoot.logControllerComp = mockUiComponent("LogController")
  FarjsRoot.devToolPanelComp = mockUiComponent("DevToolPanel")
  
  private val fileListUiF: js.Function1[Any, Unit] => Future[ReactClass] = { _ =>
    Future.successful(fileListComp)
  }

  it should "set devTool and emit resize event when on F12" in {
    //given
    val root = new FarjsRoot(fileListUiF, DevTool.Hidden)
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
    val root = new FarjsRoot(fileListUiF, DevTool.Colors)
    val onMock = mockFunction[String, js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("on" -> onMock)
    val boxMock = literal("screen" -> screen)
    onMock.expects("keypress", *)

    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    })
    val devToolProps = {
      val logProps = findComponentProps(renderer.root, logControllerComp, plain = true)
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      findComponentProps(renderedContent, devToolPanelComp, plain = true)
    }
    devToolProps.devTool shouldBe DevTool.Colors

    //when
    devToolProps.onActivate(DevTool.Logs)
    
    //then
    val updatedProps = {
      val logProps = findComponentProps(renderer.root, logControllerComp, plain = true)
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      findComponentProps(renderedContent, devToolPanelComp, plain = true)
    }
    updatedProps.devTool shouldBe DevTool.Logs
  }

  it should "render fileListUi when onReady" in {
    //given
    val root = new FarjsRoot(fileListUiF, DevTool.Hidden)
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
      <(logControllerComp())(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(onReady, render) =>
          render("test log content") shouldBe null

          //when
          onReady()
      }))()
    ))

    //then
    eventually {
      assertComponents(renderer.root.children, List(
        <.box(^.rbWidth := "100%")(
          <(fileListComp)()(
            <(taskControllerComp())(^.assertWrapped(inside(_) {
              case TaskManagerProps(currentTask) => currentTask shouldBe None
            }))()
          )
        ),
        <(logControllerComp()).empty
      ))
    }
  }
  
  it should "render component without DevTools" in {
    //given
    val root = new FarjsRoot(fileListUiF, DevTool.Hidden)
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
      <(logControllerComp())(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(_, render) =>
          render("test log content") shouldBe null
      }))()
    ))
  }
  
  it should "render component with DevTools" in {
    //given
    val root = new FarjsRoot(fileListUiF, DevTool.Logs)
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
      <(logControllerComp())(^.assertPlain[LogControllerProps](inside(_) {
        case LogControllerProps(_, render) =>
          val content = "test log content"

          assertNativeComponent(createTestRenderer(render(content)).root,
            <.box(
              ^.rbWidth := "30%",
              ^.rbHeight := "100%",
              ^.rbLeft := "70%"
            )(
              <(devToolPanelComp())(^.assertPlain[DevToolPanelProps](inside(_) {
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
