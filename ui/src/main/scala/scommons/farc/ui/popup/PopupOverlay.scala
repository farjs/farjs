package scommons.farc.ui.popup

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object PopupOverlay extends FunctionComponent[PopupProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val formRef = useRef[BlessedElement](null)
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      val focused = formRef.current.screen.focused
      if (props.focusable) {
        formRef.current.asInstanceOf[js.Dynamic].focusFirst()
      }
      props.onOpen()
      
      () => {
        if (!js.isUndefined(focused) && focused != null) {
          focused.focus()
        }
      }
    }, Nil)
    
    useLayoutEffect({ () =>
      val form = formRef.current.asInstanceOf[js.Dynamic]
      val listener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = { (_, _, key) =>
        if (key.full == "escape") {
          if (props.closable) {
            props.onClose()
          }
        }
      }
      
      form.on("element keypress", listener)
      () => {
        form.off("element keypress", listener)
      }
    }, List(props.closable, props.onClose.asInstanceOf[js.Any]))
    
    <.form(
      ^.reactRef := formRef,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := overlayStyle,
      ^.rbOnClick := { _ =>
        if (props.closable) {
          props.onClose()
        }
      }
    )(
      compProps.children
    )
  }
  
  private[popup] lazy val overlayStyle = new BlessedStyle {
    override val transparent = true
  }
}
