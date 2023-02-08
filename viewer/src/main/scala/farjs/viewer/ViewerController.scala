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
                                 encoding: String,
                                 size: Double)

object ViewerController extends FunctionComponent[ViewerControllerProps] {

  private[viewer] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[viewer] var viewerContent: UiComponent[ViewerContentProps] = ViewerContent
  
  protected def render(compProps: Props): ReactElement = {
    val (maybeFileReader, setFileReader) = useState(Option.empty[ViewerFileReader])
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      val fileReader = new ViewerFileReader
      val openF = fileReader.open(props.filePath).map { _ =>
        setFileReader(Some(fileReader))
      }
      openF.andThen { case Failure(NonFatal(_)) =>
        props.dispatch(FileListTaskAction(FutureTask("Opening file", openF)))
      }
      
      val cleanup: js.Function0[Unit] = { () =>
        fileReader.close()
      }
      cleanup
    }, Nil)

    <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
      <.box(
        ^.rbStyle := contentStyle
      )(
        maybeFileReader.filter(_ => width != 0 && height != 0).map { fileReader =>
          <(viewerContent())(^.wrapped := ViewerContentProps(
            inputRef = props.inputRef,
            fileReader = fileReader,
            encoding = props.encoding,
            size = props.size,
            width = width,
            height = height
          ))()
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
}
