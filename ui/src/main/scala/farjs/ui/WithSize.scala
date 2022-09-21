package farjs.ui

import farjs.ui.popup.PopupOverlay
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

object WithSize extends FunctionComponent[WithSizeProps] {

  protected def render(props: Props): ReactElement = {
    val boxRef = useRef[BlessedElement](null)
    val ((width, height), setSize) = useState((0, 0))
    
    useLayoutEffect({ () =>
      val currBox = boxRef.current
      setSize((currBox.width, currBox.height))
    }, List(width, height))
    
    <.box(
      ^.reactRef := boxRef,
      ^.rbStyle := PopupOverlay.style,
      ^.rbOnResize := { () =>
        val currBox = boxRef.current
        setSize((currBox.width, currBox.height))
      }
    )(
      props.plain.render(width, height)
    )
  }
}
