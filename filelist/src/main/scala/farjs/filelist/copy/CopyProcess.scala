package farjs.filelist.copy

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListItem
import farjs.ui.popup._
import farjs.ui.theme.Theme
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs
import scommons.nodejs.raw.Timers
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class CopyProcessProps(dispatch: Dispatch,
                            actions: FileListActions,
                            fromPath: String,
                            items: Seq[FileListItem],
                            toPath: String,
                            total: Double,
                            onDone: () => Unit)

object CopyProcess extends FunctionComponent[CopyProcessProps] {

  private[copy] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  
  private[copy] var timers: Timers = nodejs.global
  
  private case class CopyState(cancel: Boolean = false, timeSeconds: Int = 0)

  protected def render(compProps: Props): ReactElement = {
    val (state, setState) = useStateUpdater(CopyState())
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      val timerId = timers.setInterval({ () =>
        setState {
          case s if !s.cancel => s.copy(timeSeconds = s.timeSeconds + 1)
          case s => s
        }
      }, 1000)
      
      val cleanup: js.Function0[Unit] = { () =>
        timers.clearInterval(timerId)
      }
      cleanup
    }, Nil)
    
    <.>()(
      <(copyProgressPopup())(^.wrapped := CopyProgressPopupProps(
        item = "test.file",
        to = props.toPath,
        itemPercent = 25,
        total = props.total,
        totalPercent = 50,
        timeSeconds = state.timeSeconds,
        leftSeconds = 7,
        bytesPerSecond = 345123,
        onCancel = { () =>
          setState(_.copy(cancel = true))
        }
      ))(),

      if (state.cancel) Some {
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Operation has been interrupted",
          message = "Do you really want to cancel it?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.onDone()
            },
            MessageBoxAction.NO { () =>
              setState(_.copy(cancel = false))
            }
          ),
          style = Theme.current.popup.error
        ))()
      }
      else None
    )
  }
}
