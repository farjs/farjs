package farjs.viewer.quickview

import farjs.viewer._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object QuickViewFile extends FunctionComponent[QuickViewFileProps] {

  private[quickview] var viewerController: ReactClass = ViewerController

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (viewport, setViewport) = useState(js.undefined: js.UndefOr[ViewerFileViewport])

    val onKeypress: js.Function1[String, Boolean] = { key =>
      var processed = true
      key match {
        case "f3" =>
          props.inputRef.current.asInstanceOf[BlessedElement].emit("keypress", js.undefined, js.Dynamic.literal(
            name = "",
            full =
              if (props.isRight) ViewerEvent.onViewerOpenLeft
              else ViewerEvent.onViewerOpenRight
          ))
        case _ => processed = false
      }
      processed
    }

    <(viewerController)(^.plain := ViewerControllerProps(
      inputRef = props.inputRef,
      dispatch = props.dispatch,
      filePath = props.filePath,
      size = props.size,
      viewport = viewport,
      setViewport = setViewport,
      onKeypress = onKeypress
    ))()
  }
}
