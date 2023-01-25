package farjs.viewer

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class ViewerContentProps(inputRef: ReactRef[BlessedElement],
                              fileReader: ViewerFileReader,
                              encoding: String,
                              width: Int,
                              height: Int)

object ViewerContent extends FunctionComponent[ViewerContentProps] {
  
  private[viewer] var viewerInput: UiComponent[ViewerInputProps] = ViewerInput

  protected def render(compProps: Props): ReactElement = {
    val (position, setPosition) = useState(0.0)
    val (pageBytes, setPageBytes) = useState(0)
    val (content, setContent) = useState("")
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      props.fileReader.readPageAt(position, props.encoding).map { case (page, pageBytes) =>
        setPageBytes(pageBytes)
        setContent(page)
      }
      ()
    }, List(props.encoding))

    <(viewerInput())(^.wrapped := ViewerInputProps(
      inputRef = props.inputRef,
      onWheel = { up =>
        //TODO:
      },
      onKeypress = { keyFull =>
        //TODO:
      }
    ))(
      <.text(
        ^.rbStyle := ViewerController.contentStyle,
        ^.content := content
      )()
    )
  }
}
