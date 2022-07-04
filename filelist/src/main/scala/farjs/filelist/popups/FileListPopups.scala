package farjs.filelist.popups

import farjs.filelist.copy.CopyItems
import farjs.filelist.stack.WithPanelStacks
import farjs.filelist.{FileListData, FileListState}
import scommons.react._
import scommons.react.redux.Dispatch

case class FileListPopupsProps(dispatch: Dispatch,
                               popups: FileListPopupsState)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  private[popups] var helpController: UiComponent[FileListPopupsProps] = HelpController
  private[popups] var exitController: UiComponent[FileListPopupsProps] = ExitController
  private[popups] var menuController: UiComponent[FileListPopupsProps] = MenuController
  private[popups] var deleteController: UiComponent[PopupControllerProps] = DeleteController
  private[popups] var makeFolderController: UiComponent[PopupControllerProps] = MakeFolderController
  private[popups] var selectController: UiComponent[PopupControllerProps] = SelectController
  private[popups] var viewItemsPopupComp: UiComponent[FileListPopupsState] = ViewItemsPopup
  private[popups] var copyItemsComp: UiComponent[FileListPopupsState] = CopyItems

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped
    val maybeCurrData = {
      val stackItem = stacks.activeStack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state).map { case ((dispatch, actions), state) =>
        FileListData(dispatch, actions, state)
      }
    }
    val controllerProps = PopupControllerProps(maybeCurrData, props.popups)

    <.>()(
      <(helpController())(^.wrapped := props)(),
      <(exitController())(^.wrapped := props)(),
      <(menuController())(^.wrapped := props)(),
      
      <(deleteController())(^.wrapped := controllerProps)(),
      <(makeFolderController())(^.wrapped := controllerProps)(),
      <(selectController())(^.wrapped := controllerProps)(),

      <(viewItemsPopupComp())(^.wrapped := props.popups)(),
      <(copyItemsComp())(^.wrapped := props.popups)()
    )
  }
}
