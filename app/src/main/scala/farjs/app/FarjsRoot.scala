package farjs.app

import farjs.app.FarjsRoot._
import farjs.ui.task._
import farjs.ui.theme.Theme
import farjs.ui.tool._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

class FarjsRoot(loadMainUi: js.Function1[Any, Unit] => Future[(Theme, ReactClass)],
                initialDevTool: DevTool,
                defaultTheme: Theme
               ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val (currTheme, setCurrTheme) = useState(defaultTheme)
    val (maybeMainUi, setMainUi) = useState(Option.empty[ReactClass])
    val elementRef = useRef[BlessedElement](null)
    val (devTool, setDevTool) = useStateUpdater(initialDevTool)
    val (state, dispatch) = useReducer(TaskReducer, js.undefined: js.UndefOr[Task])

    useLayoutEffect({ () =>
      val screen = elementRef.current.screen
      val keyListener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
        if (key.full == "f12") {
          setDevTool { from =>
            val to = DevTool.getNext(from)
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

    <(Theme.Context.Provider)(^.contextValue := currTheme)(
      <.box(
        ^.reactRef := elementRef,
        ^.rbWidth := {
          if (devTool == DevTool.Hidden) "100%"
          else "70%"
        }
      )(
        maybeMainUi match {
          case None => <.text()("Loading...")
          case Some(mainComp) =>
            <(mainComp)()(
              <(taskControllerComp)(^.plain := TaskManagerProps(state))()
            )
        }
      ),
      
      <(logControllerComp)(^.plain := LogControllerProps(
        onReady = { () =>
          loadMainUi(dispatch.asInstanceOf[js.Function1[Any, Unit]]).map { case (theme, mainUi) =>
            setCurrTheme(theme)
            setMainUi(Some(mainUi))
          }
        },
        render = { content =>
          if (devTool != DevTool.Hidden) {
            <.box(
              ^.rbWidth := "30%",
              ^.rbHeight := "100%",
              ^.rbLeft := "70%"
            )(<(devToolPanelComp)(^.plain := DevToolPanelProps(
              devTool = devTool,
              logContent = content,
              onActivate = { tool =>
                setDevTool(_ => tool)
              }
            ))())
          }
          else null
        }
      ))()
    )
  }
}

object FarjsRoot {

  private[app] var taskControllerComp: ReactClass = {
    //overwrite default error handler to handle scala/java exceptions
    TaskManager.errorHandler = TaskError.errorHandler
    TaskManager
  }
  private[app] var logControllerComp: ReactClass = LogController
  private[app] var devToolPanelComp: ReactClass = DevToolPanel
}
