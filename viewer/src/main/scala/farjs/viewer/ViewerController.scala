package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.ui.theme.Theme
import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.Failure
import scala.util.control.NonFatal

case class ViewerControllerProps(inputRef: ReactRef[BlessedElement],
                                 dispatch: Dispatch,
                                 filePath: String,
                                 size: Double,
                                 viewport: Option[ViewerFileViewport],
                                 setViewport: js.Function1[Option[ViewerFileViewport], Unit] = _ => ())

object ViewerController extends FunctionComponent[ViewerControllerProps] {

  private[viewer] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[viewer] var viewerContent: UiComponent[ViewerContentProps] = ViewerContent
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      if (props.viewport.isEmpty) {
        val fileReader = new ViewerFileReader
        val openF = fileReader.open(props.filePath).map { _ =>
          props.setViewport(Some(ViewerFileViewport(
            fileReader = fileReader,
            encoding = "utf-8",
            size = props.size,
            width = 0,
            height = 0
          )))
        }
        openF.andThen { case Failure(NonFatal(_)) =>
          props.dispatch(FileListTaskAction(FutureTask("Opening file", openF)))
        }
      }
      ()
    }, Nil)

    <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
      <.box(
        ^.rbStyle := contentStyle
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
              setViewport = props.setViewport
            ))(),

            if (viewport.column > 0 && linesCount > 0) Some {
              <.text(
                ^.key := "leftScrollIndicators",
                ^.rbStyle := scrollStyle,
                ^.rbWidth := 1,
                ^.rbHeight := linesCount,
                ^.content := "<" * linesCount
              )()
            }
            else None,

            viewport.scrollIndicators.map { lineIdx =>
              <.text(
                ^.key := s"$lineIdx",
                ^.rbStyle := scrollStyle,
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

  private[viewer] lazy val contentStyle: BlessedStyle = {
    val style = Theme.current.fileList.regularItem
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
  private[viewer] lazy val scrollStyle: BlessedStyle = {
    val style = Theme.current.fileList.header
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
}
