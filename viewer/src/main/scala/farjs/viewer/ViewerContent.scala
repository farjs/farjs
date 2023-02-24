package farjs.viewer

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

case class ViewerContentProps(inputRef: ReactRef[BlessedElement],
                              viewport: ViewerFileViewport,
                              setViewport: js.Function1[Option[ViewerFileViewport], Unit])

object ViewerContent extends FunctionComponent[ViewerContentProps] {
  
  private[viewer] var viewerInput: UiComponent[ViewerInputProps] = ViewerInput

  protected def render(compProps: Props): ReactElement = {
    val readF = useRef(Future.unit)
    val props = compProps.wrapped
    val viewport = props.viewport
    
    def updated(viewport: ViewerFileViewport): Unit = {
      props.setViewport(Some(viewport))
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
      readF.current = readF.current.andThen { _ =>
        readF.current = viewport.reload(from).map(updated)
      }
    }
    
    def onWrap(): Unit = {
      readF.current = readF.current.andThen { _ =>
        val wrap = !viewport.wrap
        val column =
          if (wrap) 0
          else viewport.column

        updated(viewport.copy(wrap = wrap, column = column))
      }
    }
    
    def onEncoding(): Unit = {
      readF.current = readF.current.andThen { _ =>
        val encoding =
          if (viewport.encoding == ViewerController.defaultEnc) ViewerController.latin1Enc
          else ViewerController.defaultEnc

        updated(viewport.copy(encoding = encoding))
      }
    }
    
    def onColumn(dx: Int): Unit = {
      readF.current = readF.current.andThen { _ =>
        val col = viewport.column + dx
        if (col >= 0 && col < 1000) {
          updated(viewport.copy(column = col))
        }
      }
    }
    
    def onKeypress(keyFull: String): Unit = keyFull match {
      case "f2" => onWrap()
      case "f8" => onEncoding()
      case "left" => onColumn(dx = -1)
      case "right" => onColumn(dx = 1)
      case "C-r" => onReload()
      case "home" => onReload(from = 0)
      case "end" => onMoveUp(lines = viewport.height, from = viewport.size)
      case "up" => onMoveUp(lines = 1)
      case "pageup" => onMoveUp(lines = viewport.height)
      case "down" => onMoveDown(lines = 1)
      case "pagedown" => onMoveDown(lines = viewport.height)
      case _ =>
    }
    
    useLayoutEffect({ () =>
      readF.current = readF.current.andThen { _ =>
        readF.current = viewport.reload().map(updated)
      }
      ()
    }, List(viewport.encoding, viewport.size, viewport.width, viewport.height, viewport.wrap))

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
