package farjs.viewer

import farjs.file.popups._
import farjs.filelist.theme.FileListTheme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

case class ViewerContentProps(inputRef: ReactRef[BlessedElement],
                              viewport: ViewerFileViewport,
                              setViewport: js.Function1[Option[ViewerFileViewport], Unit],
                              onKeypress: String => Boolean)

object ViewerContent extends FunctionComponent[ViewerContentProps] {
  
  private[viewer] var viewerInput: UiComponent[ViewerInputProps] = ViewerInput
  private[viewer] var encodingsPopup: UiComponent[EncodingsPopupProps] = EncodingsPopup
  private[viewer] var textSearchPopup: UiComponent[TextSearchPopupProps] = TextSearchPopup
  private[viewer] var viewerSearch: UiComponent[ViewerSearchProps] = ViewerSearch

  protected def render(compProps: Props): ReactElement = {
    val theme = FileListTheme.useTheme()
    val readF = useRef(Future.unit)
    val (showEncodingsPopup, setShowEncodingsPopup) = useState(false)
    val (showSearchPopup, setShowSearchPopup) = useState(false)
    val (searchTerm, setSearchTerm) = useState("")
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
    
    def onEncoding(encoding: String): Unit = {
      readF.current = readF.current.andThen { _ =>
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
    
    def onKeypress(keyFull: String): Unit = {
      if (!props.onKeypress(keyFull)) {
        keyFull match {
          case "f2" => onWrap()
          case "f7" => setShowSearchPopup(true)
          case "f8" => setShowEncodingsPopup(true)
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
      }
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
        ^.rbWidth := viewport.width,
        ^.rbHeight := viewport.height,
        ^.rbStyle := ViewerController.contentStyle(theme),
        ^.rbWrap := false,
        ^.content := viewport.content
      )(),

      if (showEncodingsPopup) Some {
        <(encodingsPopup())(^.wrapped := EncodingsPopupProps(
          encoding = props.viewport.encoding,
          onApply = onEncoding,
          onClose = { () =>
            setShowEncodingsPopup(false)
          }
        ))()
      }
      else None,

      if (showSearchPopup) Some {
        <(textSearchPopup())(^.wrapped := TextSearchPopupProps(
          onSearch = { term =>
            setShowSearchPopup(false)
            setSearchTerm(term)
          },
          onCancel = { () =>
            setShowSearchPopup(false)
          }
        ))()
      }
      else None,

      if (searchTerm.nonEmpty) Some {
        <(viewerSearch())(^.wrapped := ViewerSearchProps(
          searchTerm = searchTerm,
          onComplete = { () =>
            setSearchTerm("")
          }
        ))()
      }
      else None
    )
  }
}
