package farjs.ui.popup

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object PopupOverlay extends FunctionComponent[PopupProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val formRef = useRef[BlessedElement](null)
    val props = compProps.plain

    useLayoutEffect({ () =>
      if (props.focusable.getOrElse(true)) {
        formRef.current.asInstanceOf[js.Dynamic].focusFirst()
      }
      
      props.onOpen.foreach(_.apply())
    }, Nil)
    
    useLayoutEffect({ () =>
      val form = formRef.current.asInstanceOf[js.Dynamic]
      val keyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = { (_, _, key) =>
        val keyFull = key.full
        if (!key.defaultPrevented.getOrElse(false) && !props.onKeypress.map(_.apply(keyFull)).getOrElse(false)) {
          keyFull match {
            case "escape" => props.onClose.foreach(_.apply())
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
    }, List(props.onClose, props.onKeypress))
    
    <.form(
      ^.reactRef := formRef,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := style,
      ^.rbOnClick := { _ =>
        props.onClose.foreach(_.apply())
      }
    )(
      compProps.children
    )
  }
  
  lazy val style: BlessedStyle = new BlessedStyle {
    override val transparent = true
  }
}
