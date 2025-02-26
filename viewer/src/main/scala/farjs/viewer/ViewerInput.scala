package farjs.viewer

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object ViewerInput extends FunctionComponent[ViewerInputProps] {

  protected def render(compProps: Props): ReactElement = {
    val propsRef = useRef[ViewerInputProps](null)
    val props = compProps.plain
    val inputEl = props.inputRef.current.asInstanceOf[BlessedElement]
    propsRef.current = props
    
    useLayoutEffect({ () =>
      if (inputEl != null) {
        val keyListener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
          propsRef.current.onKeypress(key.full)
        }
        val wheelupListener: js.Function1[MouseData, Unit] = { _ =>
          propsRef.current.onWheel(true)
        }
        val wheeldownListener: js.Function1[MouseData, Unit] = { _ =>
          propsRef.current.onWheel(false)
        }

        inputEl.on("keypress", keyListener)
        inputEl.on("wheelup", wheelupListener)
        inputEl.on("wheeldown", wheeldownListener)

        val cleanup: js.Function0[Unit] = { () =>
          inputEl.off("keypress", keyListener)
          inputEl.off("wheelup", wheelupListener)
          inputEl.off("wheeldown", wheeldownListener)
        }
        cleanup
      }
      else ()
    }, List(inputEl))
    
    if (js.isUndefined(compProps.children)) null
    else compProps.children
  }
}
