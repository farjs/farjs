package farjs.app

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
      <(LogController())(^.wrapped := LogControllerProps { content =>
        <.>()(
          if (devTools) Some(
            <.box(
              ^.rbWidth := "30%",
              ^.rbHeight := "100%",
              ^.rbLeft := "70%"
            )(
              <(LogPanel())(^.wrapped := LogPanelProps(content))()
              //<(ColorPanel())()()
            )
          )
          else None
        )
      })()
    )
  }
}
