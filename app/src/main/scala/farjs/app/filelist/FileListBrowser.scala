package farjs.app.filelist

import farjs.filelist.FileListActions.FileListActivateAction
import farjs.filelist._
import farjs.filelist.popups.FileListPopupsActions.FileListPopupExitAction
import farjs.filelist.stack.PanelStack.StackItem
import farjs.filelist.stack._
import farjs.ui.menu.BottomMenu
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListBrowserProps(dispatch: Dispatch,
                                actions: FileListActions,
                                data: FileListsStateDef,
                                plugins: Seq[FileListPlugin] = Nil)

object FileListBrowser extends FunctionComponent[FileListBrowserProps] {

  private[filelist] var panelStackComp: UiComponent[PanelStackProps] = PanelStack
  private[filelist] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[filelist] var bottomMenuComp: UiComponent[Unit] = BottomMenu

  protected def render(compProps: Props): ReactElement = {
    val leftButtonRef = useRef[BlessedElement](null)
    val rightButtonRef = useRef[BlessedElement](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    val props = compProps.wrapped
    val activeList = props.data.activeList
    
    val (leftStackData, setLeftStackData) = useStateUpdater(List.empty[StackItem])
    val (rightStackData, setRightStackData) = useStateUpdater(List.empty[StackItem])
    val leftStack = new PanelStack(leftStackData.headOption, setLeftStackData)
    val rightStack = new PanelStack(rightStackData.headOption, setRightStackData)

    useLayoutEffect({ () =>
      val element = 
        if (activeList.isRight) rightButtonRef.current
        else leftButtonRef.current
      
      element.focus()
    }, Nil)
    
    def getState(isRight: Boolean): FileListState = {
      if (isRight) props.data.right
      else props.data.left
    }
    
    def onActivate(isRight: Boolean): js.Function0[Unit] = { () =>
      val state = getState(isRight)
      if (!state.isActive) {
        props.dispatch(FileListActivateAction(state.isRight))
      }
    }
    
    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      val screen = leftButtonRef.current.screen
      key.full match {
        case "f10" => props.dispatch(FileListPopupExitAction(show = true))
        case "tab" | "S-tab" => screen.focusNext()
        case "C-u" =>
          setIsRight(!_)
          screen.focusNext()
        case "C-r" =>
          props.dispatch(props.actions.updateDir(props.dispatch, activeList.isRight, activeList.currDir.path))
        case keyFull =>
          props.plugins.find(_.triggerKey == keyFull).foreach { plugin =>
            plugin.onTrigger(activeList.isRight, leftStack, rightStack)
          }
      }
    }
    
    <(WithPanelStacks())(^.wrapped := WithPanelStacksProps(leftStack, rightStack))(
      <.button(
        ^("isRight") := false,
        ^.reactRef := leftButtonRef,
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbOnFocus := onActivate(isRight),
        ^.rbOnKeypress := onKeypress
      )(
        <(panelStackComp())(^.wrapped := PanelStackProps(isRight, leftButtonRef.current))(
          <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions, getState(isRight)))()
        )
      ),
      <.button(
        ^("isRight") := true,
        ^.reactRef := rightButtonRef,
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%",
        ^.rbOnFocus := onActivate(!isRight),
        ^.rbOnKeypress := onKeypress
      )(
        <(panelStackComp())(^.wrapped := PanelStackProps(!isRight, rightButtonRef.current))(
          <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions, getState(!isRight)))()
        )
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp())()()
      )
    )
  }
}
