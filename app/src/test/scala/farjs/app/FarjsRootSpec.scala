package farjs.app

import farjs.app.FarjsRoot._
import farjs.app.FarjsRootSpec._
import farjs.ui._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FarjsRootSpec extends TestSpec with TestRendererUtils {

  private val withPortalsComp: UiComponent[Unit] = () => "WithPortals".asInstanceOf[ReactClass]
  private val fileListComp = "FileListBrowser".asInstanceOf[ReactClass]
  private val fileListPopups = "FileListPopups".asInstanceOf[ReactClass]
  private val taskController = "TaskController".asInstanceOf[ReactClass]
  
  FarjsRoot.portalComp = () => "Portal".asInstanceOf[ReactClass]
  FarjsRoot.logControllerComp = () => "LogController".asInstanceOf[ReactClass]
  FarjsRoot.logPanelComp = () => "LogPanel".asInstanceOf[ReactClass]

  it should "emit resize event when on F12" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, fileListPopups, taskController, showDevTools = false)
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
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    })
    findComponents(renderer.root.children(0), <.box.name).head.props.width shouldBe "100%"
    
    //when
    TestRenderer.act { () =>
      keyListener(null, null)
    }

    //then
    findComponents(renderer.root.children(0), <.box.name).head.props.width shouldBe "70%"
    
    //cleanup
    TestRenderer.act { () =>
      renderer.unmount()
    }
  }

  it should "render component without DevTools" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, fileListPopups, taskController, showDevTools = false)
    val screenMock = mock[BlessedScreenMock]
    val boxMock = mock[BlessedElementMock]

    (boxMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    inside(result.children.toList) { case List(main, log) =>
      assertNativeComponent(main, <.box(^.rbWidth := "100%")(), { case List(withPortals) =>
        assertNativeComponent(withPortals, <(withPortalsComp())()(), { case List(portal, fileList, task) =>
          assertTestComponent(portal, portalComp) { case PortalProps(content) =>
            assertNativeComponent(createTestRenderer(content).root, <(fileListComp).empty)
          }
          assertNativeComponent(fileList, <(fileListPopups).empty)
          assertNativeComponent(task, <(taskController).empty)
        })
      })

      assertTestComponent(log, logControllerComp) { case LogControllerProps(render) =>
        render("test log content") shouldBe null
      }
    }
  }
  
  it should "render component with LogPanel" in {
    //given
    val root = new FarjsRoot(withPortalsComp, fileListComp, fileListPopups, taskController, showDevTools = true)
    val screenMock = mock[BlessedScreenMock]
    val boxMock = mock[BlessedElementMock]

    (boxMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)

    //when
    val result = createTestRenderer(<(root())()(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    inside(result.children.toList) { case List(main, log) =>
      assertNativeComponent(main, <.box(^.rbWidth := "70%")(), { case List(withPortals) =>
        assertNativeComponent(withPortals, <(withPortalsComp())()(), { case List(portal, fileList, task) =>
          assertTestComponent(portal, portalComp) { case PortalProps(content) =>
            assertNativeComponent(createTestRenderer(content).root, <(fileListComp).empty)
          }
          assertNativeComponent(fileList, <(fileListPopups).empty)
          assertNativeComponent(task, <(taskController).empty)
        })
      })

      assertTestComponent(log, logControllerComp) { case LogControllerProps(render) =>
        val content = "test log content"
        val box = createTestRenderer(render(content)).root
        
        assertNativeComponent(box,
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(), { case List(logPanel) =>
            assertTestComponent(logPanel, logPanelComp) { case LogPanelProps(resContent) =>
              resContent shouldBe content
            }
          }
        )
      }
    }
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
