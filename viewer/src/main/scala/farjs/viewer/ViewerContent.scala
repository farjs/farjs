package farjs.viewer

import farjs.file.popups._
import farjs.filelist.theme.FileListTheme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object ViewerContent extends FunctionComponent[ViewerContentProps] {
  
  private[viewer] var viewerInput: ReactClass = ViewerInput
  private[viewer] var encodingsPopup: ReactClass = EncodingsPopup
  private[viewer] var textSearchPopup: ReactClass = TextSearchPopup
  private[viewer] var viewerSearch: ReactClass = ViewerSearch

  protected def render(compProps: Props): ReactElement = {
    val theme = FileListTheme.useTheme()
    val props = compProps.plain
    val viewport = props.viewport
    val readF = useRef(js.Promise.resolve[ViewerFileViewport](viewport))
    val (showEncodingsPopup, setShowEncodingsPopup) = useState(false)
    val (showSearchPopup, setShowSearchPopup) = useState(false)
    val (searchTerm, setSearchTerm) = useState("")
    
    val updated: js.Function1[ViewerFileViewport, js.Promise[ViewerFileViewport]] = { viewport =>
      props.setViewport(viewport)
      js.Promise.resolve[ViewerFileViewport](viewport)
    }
    
    def onMoveUp(lines: Int, from: Double = viewport.position): Unit = {
      readF.current = readF.current.`then`[ViewerFileViewport](
        (viewport => viewport.moveUp(lines, from)): js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]]
      ).`then`[ViewerFileViewport](updated)
    }
    
    def onMoveDown(lines: Int): Unit = {
      readF.current = readF.current.`then`[ViewerFileViewport](
        (viewport => viewport.moveDown(lines)): js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]]
      ).`then`[ViewerFileViewport](updated)
    }
    
    def onReload(from: Double = viewport.position): Unit = {
      readF.current = readF.current.`then`[ViewerFileViewport](
        (viewport => viewport.reload(from)): js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]]
      ).`then`[ViewerFileViewport](updated)
    }
    
    def onWrap(): Unit = {
      readF.current = readF.current.`then`[ViewerFileViewport]({ viewport =>
        val wrap = !viewport.wrap
        val column =
          if (wrap) 0
          else viewport.column

        val newWrap = wrap
        val newColumn = column
        updated(viewport.updated(new ViewerFileViewportData {
          override val wrap = newWrap
          override val column = newColumn
        }))
      }: js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]])
    }
    
    val onEncoding: js.Function1[String, Unit] = { newEncoding =>
      readF.current = readF.current.`then`[ViewerFileViewport]({ viewport =>
        updated(viewport.updated(new ViewerFileViewportData {
          override val encoding = newEncoding
        }))
      }: js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]])
    }
    
    def onColumn(dx: Int): Unit = {
      readF.current = readF.current.`then`[ViewerFileViewport]({ viewport =>
        val col = viewport.column + dx
        if (col >= 0 && col < 1000) {
          updated(viewport.updated(new ViewerFileViewportData {
            override val column = col
          }))
        }
        else js.Promise.resolve[ViewerFileViewport](viewport)
      }: js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]])
    }
    
    val onKeypress: js.Function1[String, Unit] = { keyFull =>
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
      readF.current = readF.current.`then`[ViewerFileViewport](
        (_ => viewport.reload()): js.Function1[ViewerFileViewport, js.Thenable[ViewerFileViewport]]
      ).`then`[ViewerFileViewport](updated)
      ()
    }, List(viewport.encoding, viewport.size, viewport.width, viewport.height, viewport.wrap))

    <(viewerInput)(^.plain := ViewerInputProps(
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
        <(encodingsPopup)(^.plain := EncodingsPopupProps(
          encoding = props.viewport.encoding,
          onApply = onEncoding,
          onClose = { () =>
            setShowEncodingsPopup(false)
          }
        ))()
      }
      else None,

      if (showSearchPopup) Some {
        <(textSearchPopup)(^.plain := TextSearchPopupProps(
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
        <(viewerSearch)(^.plain := ViewerSearchProps(
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
