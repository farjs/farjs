package farjs.viewer.quickview

import farjs.filelist.stack.PanelStackProps
import farjs.ui.Dispatch
import farjs.viewer._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class QuickViewFileProps(dispatch: Dispatch,
                              panelStack: PanelStackProps,
                              filePath: String,
                              size: Double)

object QuickViewFile extends FunctionComponent[QuickViewFileProps] {

  private[quickview] var viewerController: UiComponent[ViewerControllerProps] = ViewerController

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (viewport, setViewport) = useState(Option.empty[ViewerFileViewport])
    val inputRef = useRef[BlessedElement](props.panelStack.panelInput)

    <(viewerController())(^.wrapped := ViewerControllerProps(
      inputRef = inputRef,
      dispatch = props.dispatch,
      filePath = props.filePath,
      size = props.size,
      viewport = viewport,
      setViewport = setViewport
    ))()
  }
}
