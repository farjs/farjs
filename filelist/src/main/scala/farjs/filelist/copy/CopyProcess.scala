package farjs.filelist.copy

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListItem
import farjs.ui.popup._
import farjs.ui.theme.Theme
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._
import scommons.react.hooks._

case class CopyProcessProps(dispatch: Dispatch,
                            actions: FileListActions,
                            items: Seq[FileListItem],
                            toPath: String,
                            total: Double,
                            onDone: () => Unit)

object CopyProcess extends FunctionComponent[CopyProcessProps] {

  private[copy] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val (cancel, setCancel) = useState(false)
    val props = compProps.wrapped
    
    <.>()(
      <(copyProgressPopup())(^.wrapped := CopyProgressPopupProps(
        item = "test.file",
        to = props.toPath,
        itemPercent = 25,
        total = props.total,
        totalPercent = 50,
        timeSeconds = 5,
        leftSeconds = 7,
        bytesPerSecond = 345123,
        onCancel = { () =>
          setCancel(true)
        }
      ))(),

      if (cancel) Some {
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Operation has been interrupted",
          message = "Do you really want to cancel it?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.onDone()
            },
            MessageBoxAction.NO { () =>
              setCancel(false)
            }
          ),
          style = Theme.current.popup.error
        ))()
      }
      else None
    )
  }
}
