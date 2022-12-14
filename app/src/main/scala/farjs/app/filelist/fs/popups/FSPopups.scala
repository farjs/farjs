package farjs.app.filelist.fs.popups

import scommons.react._
import scommons.react.redux.Dispatch

case class FSPopupsProps(dispatch: Dispatch,
                         popups: FSPopupsState)

object FSPopups extends FunctionComponent[FSPopupsProps] {

  private[popups] var folderShortcuts: UiComponent[FSPopupsProps] = FolderShortcutsController

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <.>()(
      <(folderShortcuts())(^.wrapped := props)()
    )
  }
}
