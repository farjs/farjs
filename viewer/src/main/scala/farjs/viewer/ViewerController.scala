package farjs.viewer

import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.file.{Encoding, FileReader, FileViewHistory, FileViewHistoryParams}
import farjs.filelist.history.HistoryProvider
import farjs.filelist.theme.FileListTheme
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.Failure
import scala.util.control.NonFatal

object ViewerController extends FunctionComponent[ViewerControllerProps] {

  private[viewer] var withSizeComp: ReactClass = WithSize
  private[viewer] var viewerContent: UiComponent[ViewerContentProps] = ViewerContent

  private[viewer] var createFileReader: js.Function0[ViewerFileReader] =
    () => new ViewerFileReader(new FileReader())
  
  protected def render(compProps: Props): ReactElement = {
    val theme = FileListTheme.useTheme()
    val historyProvider = HistoryProvider.useHistoryProvider()
    val props = compProps.plain
    val viewportRef = useRef(props.viewport)
    viewportRef.current = props.viewport
    
    useLayoutEffect({ () =>
      val viewerFileReader = createFileReader()
      val openF = for {
        fileViewsHistory <- historyProvider.get(fileViewsHistoryKind).toFuture
        historyItem = FileViewHistory.pathToItem(props.filePath, isEdit = false)
        maybeHistory <- fileViewsHistory.getOne(historyItem).toFuture
        _ <- viewerFileReader.open(props.filePath).toFuture
      } yield {
        val history = maybeHistory.toOption.flatMap(h => FileViewHistory.fromHistory(h).toOption)
        props.setViewport(ViewerFileViewport(
          fileReader = viewerFileReader,
          encoding = history.map(_.params.encoding).getOrElse(Encoding.platformEncoding),
          size = props.size,
          width = 0,
          height = 0,
          wrap = history.flatMap(_.params.wrap.toOption).getOrElse(false),
          column = history.flatMap(_.params.column.toOption).getOrElse(0),
          position = history.map(_.params.position).getOrElse(0.0)
        ))
      }
      openF.andThen { case Failure(NonFatal(_)) =>
        props.dispatch(TaskAction(Task("Opening file", openF)))
      }

      val cleanup: js.Function0[Unit] = { () =>
        viewerFileReader.close()
        viewportRef.current.foreach { vp =>
          val history = FileViewHistory(
            path = props.filePath,
            params = FileViewHistoryParams(
              isEdit = false,
              encoding = vp.encoding,
              position = vp.position,
              wrap = vp.wrap,
              column = vp.column
            )
          )
          for {
            fileViewsHistory <- historyProvider.get(fileViewsHistoryKind).toFuture
            _ <- fileViewsHistory.save(FileViewHistory.toHistory(history)).toFuture
          } yield ()
        }
      }
      cleanup
    }, Nil)

    <(withSizeComp)(^.plain := WithSizeProps { (width, height) =>
      <.box(
        ^.rbStyle := contentStyle(theme)
      )(
        props.viewport.toOption.map { viewport =>
          val linesCount = viewport.linesData.size
          
          <.>()(
            <(viewerContent())(^.plain := ViewerContentProps(
              inputRef = props.inputRef,
              viewport = viewport.copy(
                width = width,
                height = height
              ),
              setViewport = props.setViewport,
              onKeypress = props.onKeypress
            ))(),

            if (viewport.column > 0 && linesCount > 0) Some {
              <.text(
                ^.key := "leftScrollIndicators",
                ^.rbStyle := scrollStyle(theme),
                ^.rbWidth := 1,
                ^.rbHeight := linesCount,
                ^.content := "<" * linesCount
              )()
            }
            else None,

            viewport.scrollIndicators.map { lineIdx =>
              <.text(
                ^.key := s"$lineIdx",
                ^.rbStyle := scrollStyle(theme),
                ^.rbLeft := width - 1,
                ^.rbTop := lineIdx,
                ^.rbWidth := 1,
                ^.rbHeight := 1,
                ^.content := ">"
              )()
            }.toList
          )
        }
      )
    })()
  }

  private[viewer] def contentStyle(theme: FileListTheme): BlessedStyle = {
    val style = theme.fileList.regularItem
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
  private[viewer] def scrollStyle(theme: FileListTheme): BlessedStyle = {
    val style = theme.fileList.header
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
}
