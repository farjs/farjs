package farjs.app.util

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

case class LogControllerProps(render: String => ReactElement)

object LogController extends FunctionComponent[LogControllerProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    val (content, setContent) = useStateUpdater("")
    val oldLogRef = useRef[js.Any](null)
    val oldErrorRef = useRef[js.Any](null)
    
    useLayoutEffect({ () =>
      oldLogRef.current = g.console.log
      oldErrorRef.current = g.console.error
      val log: js.Function1[String, Unit] = { msg: String =>
        setContent(c => c + s"$msg\n")
      }
      g.console.log = log
      g.console.error = log

      val cleanup: js.Function0[Unit] = { () =>
        g.console.log = oldLogRef.current
        g.console.error = oldErrorRef.current
      }
      cleanup
    }, Nil)
    
    props.render(content)
  }
}
