package farjs.app.filelist

import farjs.filelist.FileListActions.FileListActivateAction
import farjs.filelist._
import farjs.filelist.fs.{FSDrivePopup, FSDrivePopupProps}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupExitAction
import farjs.filelist.stack._
import farjs.ui.menu.BottomMenu
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.redux.Dispatch

import scala.scalajs.js

case class FileListBrowserProps(dispatch: Dispatch,
                                data: FileListsStateDef,
                                plugins: Seq[FileListPlugin] = Nil)

class FileListBrowser(fsControllerComp: ReactClass,
                      fileListPopups: ReactClass) extends FunctionComponent[FileListBrowserProps] {

  private[filelist] var panelStackComp: UiComponent[PanelStackProps] = PanelStack
  private[filelist] var fsDrivePopup: UiComponent[FSDrivePopupProps] = FSDrivePopup
  private[filelist] var bottomMenuComp: UiComponent[Unit] = BottomMenu

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val activeList = props.data.activeList
    val leftButtonRef = useRef[BlessedElement](null)
    val rightButtonRef = useRef[BlessedElement](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    val (isRightActive, setIsRightActive) = useState(activeList.isRight)
    val (showLeftDrive, setShowLeftDrive) = useState(false)
    val (showRightDrive, setShowRightDrive) = useState(false)
    
    val (leftStackData, setLeftStackData) = useStateUpdater(() => List[PanelStackItem[_]](
      PanelStackItem[FileListState](fsControllerComp, None, None, None)
    ))
    val (rightStackData, setRightStackData) = useStateUpdater(() => List[PanelStackItem[_]](
      PanelStackItem[FileListState](fsControllerComp, None, None, None)
    ))
    val leftStack = new PanelStack(!isRightActive, leftStackData, setLeftStackData)
    val rightStack = new PanelStack(isRightActive, rightStackData, setRightStackData)

    useLayoutEffect({ () =>
      val element = 
        if (isRightActive) rightButtonRef.current
        else leftButtonRef.current
      
      element.focus()
    }, Nil)
    
    def getStack(isRight: Boolean): PanelStack = {
      if (isRight) rightStack
      else leftStack
    }
    
    def getState(isRight: Boolean): FileListState = {
      if (isRight) props.data.right
      else props.data.left
    }
    
    def onActivate(isRight: Boolean): js.Function0[Unit] = { () =>
      val state = getState(isRight)
      if (!state.isActive) {
        props.dispatch(FileListActivateAction(state.isRight))
      }

      val stack = getStack(isRight)
      if (!stack.isActive) {
        setIsRightActive(isRight)
      }
    }
    
    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      def screen = leftButtonRef.current.screen
      key.full match {
        case "M-l" => setShowLeftDrive(true)
        case "M-r" => setShowRightDrive(true)
        case "f10" => props.dispatch(FileListPopupExitAction(show = true))
        case "tab" | "S-tab" => screen.focusNext()
        case "C-u" =>
          setIsRight(!_)
          screen.focusNext()
        case keyFull =>
          props.plugins.find(_.triggerKey == keyFull).foreach { plugin =>
            plugin.onTrigger(isRightActive, leftStack, rightStack)
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
        <(panelStackComp())(^.wrapped := PanelStackProps(isRight, leftButtonRef.current, getStack(isRight)))(
          if (showLeftDrive) Some {
            <(fsDrivePopup())(^.wrapped := FSDrivePopupProps(
              dispatch = props.dispatch,
              onClose = { () =>
                setShowLeftDrive(false)
              },
              showOnLeft = true
            ))()
          }
          else None
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
        <(panelStackComp())(^.wrapped := PanelStackProps(!isRight, rightButtonRef.current, getStack(!isRight)))(
          if (showRightDrive) Some {
            <(fsDrivePopup())(^.wrapped := FSDrivePopupProps(
              dispatch = props.dispatch,
              onClose = { () =>
                setShowRightDrive(false)
              },
              showOnLeft = false
            ))()
          }
          else None
        )
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp())()()
      ),

      <(fileListPopups).empty
    )
  }
}
