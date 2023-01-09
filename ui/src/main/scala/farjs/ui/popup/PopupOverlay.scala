package farjs.ui.popup

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object PopupOverlay extends FunctionComponent[PopupProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val formRef = useRef[BlessedElement](null)
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      if (props.focusable) {
        formRef.current.asInstanceOf[js.Dynamic].focusFirst()
      }
      
      props.onOpen()
    }, Nil)
    
    useLayoutEffect({ () =>
      val form = formRef.current.asInstanceOf[js.Dynamic]
      val keyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = { (_, _, key) =>
        val keyFull = key.full
        if (!key.defaultPrevented.getOrElse(false) && !props.onKeypress(keyFull)) {
          keyFull match {
            case "escape" if props.closable => props.onClose()
            case "tab" | "down" | "right" => form.focusNext()
            case "S-tab" | "up" | "left" => form.focusPrevious()
            case _ =>
          }
        }
      }
      val focusListener: js.Function1[BlessedElement, Unit] = { el =>
        form._selected = el
      }
      
      form.on("element keypress", keyListener)
      form.on("element focus", focusListener)
      () => {
        form.off("element keypress", keyListener)
        form.off("element focus", focusListener)
      }
    }, List(props.closable, props.onClose.asInstanceOf[js.Any], props.onKeypress.asInstanceOf[js.Any]))
    
    <.form(
      ^.reactRef := formRef,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := style,
      ^.rbOnClick := { _ =>
        if (props.closable) {
          props.onClose()
        }
      }
    )(
      compProps.children
    )
  }
  
  lazy val style: BlessedStyle = new BlessedStyle {
    override val transparent = true
  }
}
