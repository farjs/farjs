package farjs.viewer

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ViewerContentProps(inputRef: ReactRef[BlessedElement],
                              fileReader: ViewerFileReader,
                              encoding: String,
                              size: Double,
                              width: Int,
                              height: Int,
                              onViewProgress: Int => Unit)

object ViewerContent extends FunctionComponent[ViewerContentProps] {
  
  private[viewer] var viewerInput: UiComponent[ViewerInputProps] = ViewerInput

  protected def render(compProps: Props): ReactElement = {
    val readF = useRef(Future.unit)
    val props = compProps.wrapped
    val (viewport, setViewport) = useState { () =>
      ViewerFileViewport(
        fileReader = props.fileReader,
        encoding = props.encoding,
        size = props.size,
        width = props.width,
        height = props.height
      )
    }
    
    def updated(viewport: ViewerFileViewport): Unit = {
      setViewport(viewport)
      props.onViewProgress(viewport.progress)
    }
    
    def onMoveUp(lines: Int, from: Double = viewport.position): Unit = {
      if (readF.current.isCompleted) {
        readF.current = viewport.moveUp(lines, from).map(updated)
      }
    }
    
    def onMoveDown(lines: Int): Unit = {
      if (readF.current.isCompleted) {
        readF.current = viewport.moveDown(lines).map(updated)
      }
    }
    
    def onReload(from: Double = viewport.position): Unit = {
      readF.current.onComplete { _ =>
        readF.current = viewport.reload(from).map(updated)
      }
    }
    
    def onKeypress(keyFull: String): Unit = keyFull match {
      case "C-r" => onReload()
      case "home" => onReload(from = 0)
      case "end" => onMoveUp(lines = props.height, from = props.size)
      case "up" => onMoveUp(lines = 1)
      case "pageup" => onMoveUp(lines = props.height)
      case "down" => onMoveDown(lines = 1)
      case "pagedown" => onMoveDown(lines = props.height)
      case _ =>
    }
    
    useLayoutEffect({ () =>
      readF.current.onComplete { _ =>
        val newViewport = viewport.copy(
          encoding = props.encoding,
          size = props.size,
          width = props.width,
          height = props.height
        )
        readF.current = newViewport.reload().map(updated)
      }
      ()
    }, List(props.encoding, props.size, props.width, props.height))

    <(viewerInput())(^.wrapped := ViewerInputProps(
      inputRef = props.inputRef,
      onWheel = { up =>
        if (up) onMoveUp(lines = 1)
        else onMoveDown(lines = 1)
      },
      onKeypress = onKeypress
    ))(
      <.text(
        ^.rbStyle := ViewerController.contentStyle,
        ^.content := viewport.content
      )()
    )
  }
}
