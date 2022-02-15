package farjs.filelist.popups

import farjs.filelist.FileListState
import farjs.filelist.copy.CopyItems
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FileListPopups extends FunctionComponent[FileListPopupsState] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  private[popups] var makeFolderPopupComp: UiComponent[MakeFolderPopupProps] = MakeFolderPopup
  private[popups] var viewItemsPopupComp: UiComponent[FileListPopupsState] = ViewItemsPopup
  private[popups] var copyItemsComp: UiComponent[FileListPopupsState] = CopyItems

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (folderName, setFolderName) = useState("")
    val (multiple, setMultiple) = useState(false)
    val props = compProps.wrapped
    val maybeCurrData = {
      val stackItem = stacks.activeStack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state)
    }
    val theme = Theme.current.popup

    maybeCurrData.map { case ((dispatch, actions), state) =>
      <.>()(
        if (props.showHelpPopup) Some(
          <(messageBoxComp())(^.wrapped := MessageBoxProps(
            title = "Help",
            message = "//TODO: show help/about info",
            actions = List(MessageBoxAction.OK { () =>
              dispatch(FileListPopupHelpAction(show = false))
            }),
            style = theme.regular
          ))()
        ) else None,

        if (props.showExitPopup) Some(
          <(messageBoxComp())(^.wrapped := MessageBoxProps(
            title = "Exit",
            message = "Do you really want to exit FAR.js?",
            actions = List(
              MessageBoxAction.YES { () =>
                dispatch(FileListPopupExitAction(show = false))
                process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                  name = "e",
                  ctrl = true,
                  meta = false,
                  shift = false
                ))
              },
              MessageBoxAction.NO { () =>
                dispatch(FileListPopupExitAction(show = false))
              }
            ),
            style = theme.regular
          ))()
        ) else None,

        if (props.showDeletePopup) Some(
          <(messageBoxComp())(^.wrapped := MessageBoxProps(
            title = "Delete",
            message = "Do you really want to delete selected item(s)?",
            actions = List(
              MessageBoxAction.YES { () =>
                val items =
                  if (state.selectedItems.nonEmpty) state.selectedItems
                  else state.currentItem.toList

                dispatch(FileListPopupDeleteAction(show = false))
                dispatch(actions.deleteAction(
                  dispatch = dispatch,
                  isRight = state.isRight,
                  dir = state.currDir.path,
                  items = items
                ))
              },
              MessageBoxAction.NO { () =>
                dispatch(FileListPopupDeleteAction(show = false))
              }
            ),
            style = theme.error
          ))()
        ) else None,

        if (props.showMkFolderPopup) Some(
          <(makeFolderPopupComp())(^.wrapped := MakeFolderPopupProps(
            folderName = folderName,
            multiple = multiple,
            onOk = { (dir, multiple) =>
              val action = actions.createDir(
                dispatch = dispatch,
                isRight = state.isRight,
                parent = state.currDir.path,
                dir = dir,
                multiple = multiple
              )
              action.task.future.foreach { _ =>
                setFolderName(dir)
                setMultiple(multiple)
                dispatch(FileListPopupMkFolderAction(show = false))
              }
              dispatch(action)
            },
            onCancel = { () =>
              dispatch(FileListPopupMkFolderAction(show = false))
            }
          ))()
        ) else None,

        <(viewItemsPopupComp())(^.wrapped := props)(),
        <(copyItemsComp())(^.wrapped := props)()
      )
    }.orNull
  }
}
