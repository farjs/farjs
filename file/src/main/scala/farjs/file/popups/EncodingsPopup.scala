package farjs.file.popups

import farjs.file.Encoding
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

case class EncodingsPopupProps(encoding: String,
                               onApply: String => Unit,
                               onClose: () => Unit)

object EncodingsPopup extends FunctionComponent[EncodingsPopupProps] {

  private[file] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (maybeItems, setItems) = useState(Option.empty[List[String]])

    useLayoutEffect({ () =>
      setItems(Some(Encoding.encodings))
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.wrapped := ListPopupProps(
        title = "Encodings",
        items = items,
        onAction = { index =>
          val enc = items(index)
          if (enc != props.encoding) {
            props.onApply(enc)
          }
          props.onClose()
        },
        onClose = props.onClose,
        selected = math.max(items.indexOf(props.encoding), 0)
      ))()
    }.orNull
  }
}
