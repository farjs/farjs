package farjs.viewer

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.ui.{WithSize, WithSizeProps}
import farjs.ui.theme.Theme
import scommons.nodejs
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.{Buffer, FS}
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.control.NonFatal

case class ViewerContentProps(dispatch: Dispatch, filePath: String, encoding: String)

object ViewerContent extends FunctionComponent[ViewerContentProps] {

  private[viewer] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[viewer] var fs: FS = nodejs.fs
  
  protected def render(compProps: Props): ReactElement = {
    val (content, setContent) = useState("")
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      val buff = Buffer.allocUnsafe(64 * 1024)
      val loadF = for {
        fd <- Future(fs.openSync(props.filePath, FSConstants.O_RDONLY))
        bytesRead <- fs.read(fd, buff, offset = 0, length = buff.length, position = 0)
        _ <- Future(fs.closeSync(fd))
      } yield {
        setContent(buff.toString(props.encoding, start = 0, end = bytesRead))
      }
      loadF.andThen {
        case Failure(NonFatal(_)) => props.dispatch(FileListTaskAction(FutureTask("Reading File", loadF)))
      }
      ()
    }, Nil)

    <(withSizeComp())(^.plain := WithSizeProps { (_, _) =>
      <.box(
        ^.rbStyle := contentStyle
      )(
        <.text(
          ^.rbStyle := contentStyle,
          ^.content := content
        )()
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
