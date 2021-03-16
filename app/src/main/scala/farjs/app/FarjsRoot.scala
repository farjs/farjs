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
                fileListComp: ReactClass,
                fileListPopups: ReactClass,
                taskController: ReactClass,
                initialDevTool: DevTool
               ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val (devTool, setDevTool) = useStateUpdater(initialDevTool)

    useLayoutEffect({ () =>
      val screen = elementRef.current.screen
      screen.key(js.Array("f12"), { (_, _) =>
        setDevTool { from =>
          val to = from.getNext
          if (DevTool.shouldResize(from, to)) Future[Unit] { //exec on the next tick
            screen.program.emit("resize")
          }
          to
        }
      })
      ()
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
          <(fileListComp).empty,
          <(fileListPopups).empty,
          <(taskController).empty
        )
      ),
      
      <(logControllerComp())(^.wrapped := LogControllerProps { content =>
        val comp = devTool match {
          case DevTool.Hidden => <.>()()
          case DevTool.Logs => <(logPanelComp())(^.wrapped := LogPanelProps(content))()
          case DevTool.Colors => <(colorPanelComp())()()
        }
        
        if (devTool != DevTool.Hidden) {
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(comp)
        }
        else null
      })()
    )
  }
}

object FarjsRoot {

  private[app] var logControllerComp: UiComponent[LogControllerProps] = LogController
  private[app] var logPanelComp: UiComponent[LogPanelProps] = LogPanel
  private[app] var colorPanelComp: UiComponent[Unit] = ColorPanel
}
