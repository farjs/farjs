package farjs.viewer

import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class EncodingsPopupProps(encoding: String,
                               onApply: String => Unit,
                               onClose: () => Unit)

object EncodingsPopup extends FunctionComponent[EncodingsPopupProps] {

  private[viewer] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (origEncoding, _) = useState(props.encoding)
    val (maybeItems, setItems) = useState(Option.empty[List[String]])

    useLayoutEffect({ () =>
      setItems(Some(List(
        "utf-8",
        "latin1"
      )))
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.wrapped := ListPopupProps(
        title = "Encodings",
        items = items,
        onAction = { _ =>
          props.onClose()
        },
        onClose = { () =>
          if (origEncoding != props.encoding) {
            props.onApply(origEncoding)
          }
          props.onClose()
        },
        selected = math.max(items.indexOf(props.encoding), 0),
        onSelect = { index =>
          props.onApply(items(index))
        }: js.Function1[Int, Unit]
      ))()
    }.orNull
  }
}
