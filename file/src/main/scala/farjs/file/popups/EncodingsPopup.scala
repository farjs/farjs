package farjs.file.popups

import farjs.file.Encoding
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object EncodingsPopup extends FunctionComponent[EncodingsPopupProps] {

  private[file] var listPopup: ReactClass = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])

    useLayoutEffect({ () =>
      setItems(Some(js.Array(Encoding.encodings: _*)))
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup)(^.plain := ListPopupProps(
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
