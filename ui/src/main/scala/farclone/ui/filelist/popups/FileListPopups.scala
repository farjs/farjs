package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.popup._
import scommons.react._

case class FileListPopupsProps(dispatch: Dispatch,
                               data: FileListPopupsState)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <.>()(
      if (props.data.showHelpPopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
          title = "Help",
          message = "//TODO: show help/about info",
          actions = List(MessageBoxAction.OK { () =>
            props.dispatch(FileListHelpAction(show = false))
          })
        ))()
      ) else None
    )
  }
}
