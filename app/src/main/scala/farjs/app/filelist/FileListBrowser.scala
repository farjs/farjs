package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.app.filelist.FileListRoot.FSPlugin
import farjs.filelist._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import farjs.ui.menu._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

class FileListBrowser(pluginHandler: FileListPluginHandler) extends FunctionComponent[FileListBrowserProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val leftButtonRef = useRef[BlessedElement](null)
    val rightButtonRef = useRef[BlessedElement](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    val (isRightActive, setIsRightActive) = useState(props.isRightInitiallyActive)
    val (currPluginUi, setCurrPluginUi) = useState(Option.empty[ReactClass])
    
    val (leftStackData, setLeftStackData) = useStateUpdater(() => js.Array[PanelStackItem[_]](
      PanelStackItem[FileListState](fsPlugin.component)
    ))
    val (rightStackData, setRightStackData) = useStateUpdater(() => js.Array[PanelStackItem[_]](
      PanelStackItem[FileListState](fsPlugin.component)
    ))
    val leftStack = new PanelStack(!isRightActive, leftStackData, setLeftStackData)
    val rightStack = new PanelStack(isRightActive, rightStackData, setRightStackData)

    def getInput(isRightActive: Boolean): BlessedElement = {
      val (leftEl, rightEl) =
        if (isRight) (rightButtonRef.current, leftButtonRef.current)
        else (leftButtonRef.current, rightButtonRef.current)
        
      if (isRightActive) rightEl
      else leftEl
    }

    def getStack(isRight: Boolean): PanelStack = {
      if (isRight) rightStack
      else leftStack
    }
    
    def onActivate(isRight: Boolean): js.Function0[Unit] = { () =>
      val stack = getStack(isRight)
      if (!stack.isActive) {
        setIsRightActive(isRight)
      }
    }
    
    val stacks = WithStacksProps(
      left = WithStacksData(getStack(isRight), leftButtonRef.current),
      right = WithStacksData(getStack(!isRight), rightButtonRef.current)
    )
    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      def screen = leftButtonRef.current.screen
      key.full match {
        case "tab" | "S-tab" => screen.focusNext()
        case "C-u" =>
          setIsRight(!_)
          screen.focusNext()
        case "enter" | "C-pagedown" =>
          pluginHandler.openCurrItem(props.dispatch, getStack(isRightActive))
        case _ =>
          pluginHandler.openPluginUi(props.dispatch, key, stacks).toFuture.foreach { maybePluginUi =>
            maybePluginUi.foreach { pluginUi =>
              setCurrPluginUi(Some(pluginUi))
            }
          }
      }
    }
    
    useLayoutEffect({ () =>
      fsPlugin.init(props.dispatch, leftStack)
      fsPlugin.init(props.dispatch, rightStack)

      getInput(isRightActive).focus()
      ()
    }, Nil)

    <(WithStacks)(^.plain := stacks)(
      <.button(
        ^("isRight") := false,
        ^.reactRef := leftButtonRef,
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbOnFocus := onActivate(isRight),
        ^.rbOnKeypress := onKeypress
      )(
        <(withStackComp)(^.plain := WithStackProps(
          isRight = false,
          panelInput = leftButtonRef.current,
          stack = getStack(isRight)
        ))()
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
        <(withStackComp)(^.plain := WithStackProps(
          isRight = true,
          panelInput = rightButtonRef.current,
          stack = getStack(!isRight)
        ))()
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp)(^.plain := BottomMenuProps(menuItems))()
      ),
      <(menuBarTrigger)()(),

      currPluginUi.map { pluginUi =>
        <(pluginUi)(^.plain := FileListPluginUiProps(
          dispatch = props.dispatch,
          onClose = { () =>
            setCurrPluginUi(None)
          }
        ))()
      }
    )
  }
}

object FileListBrowser {

  private[filelist] var withStackComp: ReactClass = WithStack
  private[filelist] var bottomMenuComp: ReactClass = BottomMenu
  private[filelist] var menuBarTrigger: ReactClass = MenuBarTrigger
  private[filelist] var fsPlugin: FSPlugin = FSPlugin.instance

  private[filelist] val menuItems = js.Array(
    /*  F1 */ "",
    /*  F2 */ "",
    /*  F3 */ "View",
    /*  F4 */ "",
    /*  F5 */ "Copy",
    /*  F6 */ "RenMov",
    /*  F7 */ "MkFolder",
    /*  F8 */ "Delete",
    /*  F9 */ "Menu",
    /* F10 */ "Exit",
    /* F11 */ "",
    /* F12 */ "DevTools"
  )
}

trait FileListPluginHandler extends js.Object {

  def openCurrItem(dispatch: Dispatch, stack: PanelStack): Unit

  def openPluginUi(dispatch: Dispatch, key: KeyboardKey, stacks: WithStacksProps): js.Promise[js.UndefOr[ReactClass]]
}

@js.native
@JSImport("../app/filelist/FileListPluginHandler.mjs", JSImport.Default)
object FileListPluginHandler extends js.Function1[js.Array[FileListPlugin], FileListPluginHandler] {

  def apply(plugins: js.Array[FileListPlugin]): FileListPluginHandler = js.native
}
