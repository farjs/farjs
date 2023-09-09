package farjs.viewer

import farjs.file.{Encoding, FileServices, FileViewHistory}
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.theme.FileListTheme
import farjs.ui.task.Task
import farjs.ui.{Dispatch, WithSize, WithSizeProps}
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

  private[viewer] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[viewer] var viewerContent: UiComponent[ViewerContentProps] = ViewerContent
  
  protected def render(compProps: Props): ReactElement = {
    val theme = FileListTheme.useTheme
    val services = FileServices.useServices
    val props = compProps.wrapped
    val viewportRef = useRef(props.viewport)
    viewportRef.current = props.viewport
    
    useLayoutEffect({ () =>
      val fileReader = new ViewerFileReader
      val openF = for {
        history <- services.fileViewHistory.getOne(props.filePath, isEdit = false)
        _ <- fileReader.open(props.filePath)
      } yield {
        props.setViewport(Some(ViewerFileViewport(
          fileReader = fileReader,
          encoding = history.map(_.encoding).getOrElse(Encoding.platformEncoding),
          size = props.size,
          width = 0,
          height = 0,
          wrap = history.flatMap(_.wrap).getOrElse(false),
          column = history.flatMap(_.column).getOrElse(0),
          position = history.map(_.position).getOrElse(0.0)
        )))
      }
      openF.andThen { case Failure(NonFatal(_)) =>
        props.dispatch(FileListTaskAction(Task("Opening file", openF)))
      }

      val cleanup: js.Function0[Unit] = { () =>
        fileReader.close()
        viewportRef.current.foreach { vp =>
          services.fileViewHistory.save(FileViewHistory(
            path = props.filePath,
            isEdit = false,
            encoding = vp.encoding,
            position = vp.position,
            wrap = Some(vp.wrap),
            column = Some(vp.column)
          ))
        }
      }
      cleanup
    }, Nil)

    <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
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
