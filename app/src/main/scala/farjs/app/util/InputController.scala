package farjs.app.util

import scommons.nodejs.global
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object InputController extends FunctionComponent[Unit] {

  private val g: js.Dynamic = global.asInstanceOf[js.Dynamic]

  private[util] var logPanelComp: UiComponent[LogPanelProps] = LogPanel
  private[util] var maxBufferLength: Int = 4000
  
  protected def render(compProps: Props): ReactElement = {
    val (content, setContent) = useStateUpdater("")
    
    useLayoutEffect({ () =>
      val logKeys: js.Function1[String, Unit] = { msg: String =>
        setContent { c =>
          val buf = c + s"$msg\n"
          if (buf.length > maxBufferLength) {
            val cutAt = buf.indexOf('\n', buf.length - maxBufferLength)
            buf.substring(cutAt + 1)
          }
          else buf
        }
      }
      g.farjsLogKeys = logKeys

      val cleanup: js.Function0[Unit] = { () =>
        g.farjsLogKeys = js.undefined
      }
      cleanup
    }, Nil)

    <(logPanelComp())(^.wrapped := LogPanelProps(content))()
  }
}
