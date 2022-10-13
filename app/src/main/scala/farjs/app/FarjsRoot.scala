package farjs.app

import farjs.app.FarjsRoot._
import farjs.app.util._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

class FarjsRoot(withPortalsComp: UiComponent[Unit],
                loadFileListUi: => Future[ReactClass],
                taskController: ReactClass,
                initialDevTool: DevTool
               ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val (maybeFileListUi, setFileListUi) = useState(Option.empty[ReactClass])
    val elementRef = useRef[BlessedElement](null)
    val (devTool, setDevTool) = useStateUpdater(initialDevTool)

    useLayoutEffect({ () =>
      val screen = elementRef.current.screen
      val keyListener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
        if (key.full == "f12") {
          setDevTool { from =>
            val to = from.getNext
            if (DevTool.shouldResize(from, to)) Future[Unit] { //exec on the next tick
              screen.program.emit("resize")
            }
            to
          }
        }
      }
      screen.on("keypress", keyListener)

      val cleanup: js.Function0[Unit] = { () =>
        screen.off("keypress", keyListener)
      }
      cleanup
    }, Nil)

    <.>()(
      <.box(
        ^.reactRef := elementRef,
        ^.rbWidth := {
          if (devTool == DevTool.Hidden) "100%"
          else "70%"
        }
      )(
        <(withPortalsComp())()(
          maybeFileListUi match {
            case None => <.text()("Loading...")
            case Some(fileListComp) => <(fileListComp).empty
          },
          <(taskController).empty
        )
      ),
      
      <(logControllerComp())(^.wrapped := LogControllerProps(onReady = { () =>
        loadFileListUi.map { fileListUi =>
          setFileListUi(Some(fileListUi))
        }
      }, { content =>
        if (devTool != DevTool.Hidden) {
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(<(devToolPanelComp())(^.wrapped := DevToolPanelProps(
            devTool = devTool,
            logContent = content,
            onActivate = { tool =>
              setDevTool(_ => tool)
            }
          ))())
        }
        else null
      }))()
    )
  }
}

object FarjsRoot {

  private[app] var logControllerComp: UiComponent[LogControllerProps] = LogController
  private[app] var devToolPanelComp: UiComponent[DevToolPanelProps] = DevToolPanel
}
