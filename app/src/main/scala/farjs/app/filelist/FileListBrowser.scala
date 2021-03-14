package farjs.app.filelist

import farjs.filelist.FileListActions.FileListActivateAction
import farjs.filelist._
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
                                data: FileListsStateDef)

object FileListBrowser extends FunctionComponent[FileListBrowserProps] {

  private[filelist] var panelStackComp: UiComponent[PanelStackProps] = PanelStack
  private[filelist] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[filelist] var bottomMenuComp: UiComponent[Unit] = BottomMenu

  protected def render(compProps: Props): ReactElement = {
    val leftButtonRef = useRef[BlessedElement](null)
    val rightButtonRef = useRef[BlessedElement](null)
    val leftStackRef = useRef[PanelStack](null)
    val rightStackRef = useRef[PanelStack](null)
    val propsRef = useRef[FileListBrowserProps](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    
    val (leftStackData, setLeftStackData) = useStateUpdater(List.empty[StackItem])
    val (rightStackData, setRightStackData) = useStateUpdater(List.empty[StackItem])
    leftStackRef.current = new PanelStack(leftStackData.headOption, setLeftStackData)
    rightStackRef.current = new PanelStack(rightStackData.headOption, setRightStackData)

    useLayoutEffect({ () =>
      val element = 
        if (propsRef.current.data.activeList.isRight) rightButtonRef.current
        else leftButtonRef.current
      
      element.focus()
      
      val screen = element.screen
      screen.key(js.Array("C-u"), { (_, _) =>
        setIsRight(!_)
        screen.focusNext()
      })
      ()
    }, Nil)

    val props = compProps.wrapped
    propsRef.current = props
    
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
      val keyFull = key.full
      if (keyFull == "tab" || keyFull == "S-tab") {
        leftButtonRef.current.screen.focusNext()
      }
    }
    
    <(WithPanelStacks())(^.wrapped := WithPanelStacksProps(leftStackRef.current, rightStackRef.current))(
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
