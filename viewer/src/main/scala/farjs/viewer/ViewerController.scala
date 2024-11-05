package farjs.viewer

import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.file.{Encoding, FileReader, FileViewHistory, FileViewHistoryParams}
import farjs.filelist.history.HistoryProvider
import farjs.filelist.theme.FileListTheme
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.{Dispatch, WithSize, WithSizeProps}
import scommons.nodejs
import scommons.nodejs.FS
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.Failure
import scala.util.control.NonFatal

case class ViewerControllerProps(inputRef: ReactRef[BlessedElement],
                                 dispatch: Dispatch,
                                 filePath: String,
                                 size: Double,
                                 viewport: Option[ViewerFileViewport],
                                 setViewport: js.Function1[Option[ViewerFileViewport], Unit] = _ => (),
                                 onKeypress: String => Boolean = _ => false)

object ViewerController extends FunctionComponent[ViewerControllerProps] {

  private[viewer] var withSizeComp: ReactClass = WithSize
  private[viewer] var viewerContent: UiComponent[ViewerContentProps] = ViewerContent
  private[viewer] var fs: FS = nodejs.fs
  
  protected def render(compProps: Props): ReactElement = {
    val theme = FileListTheme.useTheme
    val historyProvider = HistoryProvider.useHistoryProvider
    val props = compProps.wrapped
    val viewportRef = useRef(props.viewport)
    viewportRef.current = props.viewport
    
    useLayoutEffect({ () =>
      val fileReader = new FileReader(fs)
      val viewer = new ViewerFileReader(fileReader)
      val openF = for {
        fileViewsHistory <- historyProvider.get(fileViewsHistoryKind).toFuture
        maybeHistory <- fileViewsHistory.getOne(props.filePath).toFuture
        _ <- fileReader.open(props.filePath)
      } yield {
        val history = maybeHistory.toOption.flatMap(FileViewHistory.fromHistory)
        props.setViewport(Some(ViewerFileViewport(
          fileReader = viewer,
          encoding = history.map(_.params.encoding).getOrElse(Encoding.platformEncoding),
          size = props.size,
          width = 0,
          height = 0,
          wrap = history.flatMap(_.params.wrap.toOption).getOrElse(false),
          column = history.flatMap(_.params.column.toOption).getOrElse(0),
          position = history.map(_.params.position).getOrElse(0.0)
        )))
      }
      openF.andThen { case Failure(NonFatal(_)) =>
        props.dispatch(TaskAction(Task("Opening file", openF)))
      }

      val cleanup: js.Function0[Unit] = { () =>
        fileReader.close()
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
        props.viewport.map { viewport =>
          val linesCount = viewport.linesData.size
          
          <.>()(
            <(viewerContent())(^.wrapped := ViewerContentProps(
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
            }
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
