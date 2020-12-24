package farjs.app

import farjs.app.FarjsRoot._
import farjs.ui._
import scommons.react._
import scommons.react.hooks._
import scommons.react.blessed._
import scommons.react.blessed.portal._

import scala.scalajs.js

class FarjsRoot(fileListComp: ReactClass,
                fileListPopups: ReactClass,
                taskController: ReactClass,
                showDevTools: Boolean
               ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val (devTools, setDevTools) = useStateUpdater(showDevTools)

    useLayoutEffect({ () =>
      val screen = elementRef.current.screen
      screen.key(js.Array("f12"), { (_, _) =>
        setDevTools(v => !v)
        screen.program.emit("resize")
      })
      ()
    }, Nil)

    <.>()(
      <.box(
        ^.reactRef := elementRef,
        if (devTools) Some(
          ^.rbWidth := "70%"
        )
        else Some(
          ^.rbWidth := "100%"
        )
      )(
        <(withPortalsComp())()(
          <(portalComp())(^.wrapped := PortalProps(
            <(fileListComp).empty
          ))(),
          <(fileListPopups).empty,
          <(taskController).empty
        )
      ),
      
      <(logControllerComp())(^.wrapped := LogControllerProps { content =>
        if (devTools) {
          <.box(
            ^.rbWidth := "30%",
            ^.rbHeight := "100%",
            ^.rbLeft := "70%"
          )(
            <(logPanelComp())(^.wrapped := LogPanelProps(content))()
            //<(ColorPanel())()()
          )
        }
        else null
      })()
    )
  }
}

object FarjsRoot {

  private[app] var withPortalsComp: UiComponent[Unit] = WithPortals
  private[app] var portalComp: UiComponent[PortalProps] = Portal
  private[app] var logControllerComp: UiComponent[LogControllerProps] = LogController
  private[app] var logPanelComp: UiComponent[LogPanelProps] = LogPanel
}
