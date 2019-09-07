package scommons.farc.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.filelist.popups.FileListPopupsActions._
import scommons.farc.ui.popup.{OkPopup, OkPopupProps}
import scommons.react._

case class FileListPopupsProps(dispatch: Dispatch,
                               data: FileListPopupsState)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <.>()(
      if (props.data.showHelpPopup) Some(
        <(OkPopup())(^.wrapped := OkPopupProps(
          title = "Help",
          message = "//TODO: show help/about info",
          onClose = { () =>
            props.dispatch(FileListHelpAction(show = false))
          }
        ))()
      ) else None
    )
  }
}
