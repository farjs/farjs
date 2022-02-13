package farjs.app

import farjs.app.FarjsRoot._
import farjs.app.util._
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FarjsRootSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  private val withPortalsComp = mockUiComponent[Unit]("WithPortals")
  private val fileListComp = mockUiComponent[Unit]("FileListBrowser").apply()
  private val taskController = mockUiComponent[Unit]("TaskController").apply()
  
  FarjsRoot.logControllerComp = mockUiComponent("LogController")
  FarjsRoot.devToolPanelComp = mockUiComponent("DevToolPanel")

  it should "set devTool and emit resize event when on F12" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, taskController, DevTool.Hidden)
    val emitMock = mockFunction[String, Unit]
    val program = literal("emit" -> emitMock)
    val keyMock = mockFunction[js.Array[String], js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("program" -> program, "key" -> keyMock)
    val boxMock = literal("screen" -> screen)
    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null

    keyMock.expects(*, *).onCall { (keys, listener) =>
      keys.toList shouldBe List("f12")
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
      keyListener(null, null)
    }

    //then
    findComponents(renderer.root.children(0), <.box.name).head.props.width shouldBe "70%"
    
    eventually {
      emitCalled shouldBe true
    }.map { _ =>
      //cleanup
      TestRenderer.act { () =>
        renderer.unmount()
      }
      Succeeded
    }
  }

  it should "set devTool when onActivate" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, taskController, DevTool.Colors)
    val keyMock = mockFunction[js.Array[String], js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("key" -> keyMock)
    val boxMock = literal("screen" -> screen)
    keyMock.expects(*, *)

    val renderer = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    })
    val devToolProps = {
      val logProps = findComponentProps(renderer.root, logControllerComp)
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      findComponentProps(renderedContent, devToolPanelComp)
    }
    devToolProps.devTool shouldBe DevTool.Colors

    //when
    devToolProps.onActivate(DevTool.Logs)
    
    //then
    val updatedProps = {
      val logProps = findComponentProps(renderer.root, logControllerComp)
      val renderedContent = createTestRenderer(logProps.render("test log content")).root
      findComponentProps(renderedContent, devToolPanelComp)
    }
    updatedProps.devTool shouldBe DevTool.Logs
  }

  it should "render component without DevTools" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, taskController, DevTool.Hidden)
    val keyMock = mockFunction[js.Array[String], js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("key" -> keyMock)
    val boxMock = literal("screen" -> screen)
    keyMock.expects(*, *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    }).root

    //then
    inside(result.children.toList) { case List(main, log) =>
      assertNativeComponent(main, <.box(^.rbWidth := "100%")(
        <(withPortalsComp())()(
          <(fileListComp).empty,
          <(taskController).empty
        )
      ))

      assertTestComponent(log, logControllerComp) { case LogControllerProps(render) =>
        render("test log content") shouldBe null
      }
    }
  }
  
  it should "render component with LogPanel" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, taskController, DevTool.Logs)
    val keyMock = mockFunction[js.Array[String], js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("key" -> keyMock)
    val boxMock = literal("screen" -> screen)
    keyMock.expects(*, *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    }).root

    //then
    inside(result.children.toList) { case List(main, log) =>
      assertNativeComponent(main, <.box(^.rbWidth := "70%")(
        <(withPortalsComp())()(
          <(fileListComp).empty,
          <(taskController).empty
        )
      ))

      assertTestComponent(log, logControllerComp) { case LogControllerProps(render) =>
        val content = "test log content"
        
        assertNativeComponent(createTestRenderer(render(content)).root,
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(), inside(_) { case List(comp) =>
            assertTestComponent(comp, devToolPanelComp) { case DevToolPanelProps(devTool, logContent, _) =>
              devTool shouldBe DevTool.Logs
              logContent shouldBe content
            }
          }
        )
      }
    }
  }
  
  it should "render component with ColorPanel" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, taskController, DevTool.Colors)
    val keyMock = mockFunction[js.Array[String], js.Function2[js.Object, KeyboardKey, Unit], Unit]
    val screen = literal("key" -> keyMock)
    val boxMock = literal("screen" -> screen)
    keyMock.expects(*, *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock
      else null
    }).root

    //then
    inside(result.children.toList) { case List(main, log) =>
      assertNativeComponent(main, <.box(^.rbWidth := "70%")(
        <(withPortalsComp())()(
          <(fileListComp).empty,
          <(taskController).empty
        )
      ))

      assertTestComponent(log, logControllerComp) { case LogControllerProps(render) =>
        val content = "test log content"
        
        assertNativeComponent(createTestRenderer(render(content)).root,
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(), inside(_) { case List(comp) =>
            assertTestComponent(comp, devToolPanelComp) { case DevToolPanelProps(devTool, logContent, _) =>
              devTool shouldBe DevTool.Colors
              logContent shouldBe content
            }
          }
        )
      }
    }
  }
}
