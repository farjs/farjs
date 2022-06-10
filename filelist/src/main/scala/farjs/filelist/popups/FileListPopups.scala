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
import scommons.react.redux.Dispatch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FileListPopupsProps(dispatch: Dispatch,
                               popups: FileListPopupsState)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  private[popups] var makeFolderPopupComp: UiComponent[MakeFolderPopupProps] = MakeFolderPopup
  private[popups] var selectPopupComp: UiComponent[SelectPopupProps] = SelectPopup
  private[popups] var viewItemsPopupComp: UiComponent[FileListPopupsState] = ViewItemsPopup
  private[popups] var copyItemsComp: UiComponent[FileListPopupsState] = CopyItems

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (folderName, setFolderName) = useState("")
    val (multiple, setMultiple) = useState(false)
    val (selectPattern, setSelectPattern) = useState("")
    val props = compProps.wrapped
    val popups = props.popups
    val maybeCurrData = {
      val stackItem = stacks.activeStack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state)
    }
    val theme = Theme.current.popup

    <.>()(
      if (popups.showHelpPopup) Some(
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Help",
          message = "//TODO: show help/about info",
          actions = List(MessageBoxAction.OK { () =>
            props.dispatch(FileListPopupHelpAction(show = false))
          }),
          style = theme.regular
        ))()
      ) else None,

      if (popups.showExitPopup) Some(
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Exit",
          message = "Do you really want to exit FAR.js?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.dispatch(FileListPopupExitAction(show = false))
              process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                name = "e",
                ctrl = true,
                meta = false,
                shift = false
              ))
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListPopupExitAction(show = false))
            }
          ),
          style = theme.regular
        ))()
      ) else None,

      maybeCurrData.map { case ((dispatch, actions), state) =>
        <.>()(
          if (popups.showDeletePopup) Some(
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
  
          if (popups.showMkFolderPopup) Some(
            <(makeFolderPopupComp())(^.wrapped := MakeFolderPopupProps(
              folderName = folderName,
              multiple = multiple,
              onOk = { (dir, multiple) =>
                val action = actions.createDir(
                  dispatch = dispatch,
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
  
          if (popups.showSelectPopup != SelectHidden) Some(
            <(selectPopupComp())(^.wrapped := SelectPopupProps(
              pattern = selectPattern,
              action = popups.showSelectPopup,
              onAction = { pattern =>
                setSelectPattern(pattern)
              },
              onCancel = { () =>
                dispatch(FileListPopupSelectAction(SelectHidden))
              }
            ))()
          ) else None,
  
          <(viewItemsPopupComp())(^.wrapped := popups)(),
          <(copyItemsComp())(^.wrapped := popups)()
        )
      }
    )
  }
}
