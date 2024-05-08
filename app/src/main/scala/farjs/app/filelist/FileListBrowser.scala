package farjs.app.filelist

import farjs.filelist._
import farjs.filelist.stack._
import farjs.fs.FSPlugin
import farjs.ui.Dispatch
import farjs.ui.menu._
import farjs.ui.task.{Task, TaskAction}
import scommons.nodejs.path
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Failure

case class FileListBrowserProps(dispatch: Dispatch,
                                isRightInitiallyActive: Boolean = false,
                                plugins: Seq[FileListPlugin] = Nil)

object FileListBrowser extends FunctionComponent[FileListBrowserProps] {

  private[filelist] var panelStackComp: UiComponent[PanelStackProps] = PanelStackComp
  private[filelist] var bottomMenuComp: ReactClass = BottomMenu
  private[filelist] var menuBarTrigger: ReactClass = MenuBarTrigger
  private[filelist] var fsPlugin: FSPlugin = FSPlugin

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val leftButtonRef = useRef[BlessedElement](null)
    val rightButtonRef = useRef[BlessedElement](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    val (isRightActive, setIsRightActive) = useState(props.isRightInitiallyActive)
    val (currPluginUi, setCurrPluginUi) = useState(Option.empty[ReactClass])
    
    val (leftStackData, setLeftStackData) = useStateUpdater(() => List[PanelStackItem[_]](
      PanelStackItem[FileListState](fsPlugin.component)
    ))
    val (rightStackData, setRightStackData) = useStateUpdater(() => List[PanelStackItem[_]](
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
    
    val stacks = WithPanelStacksProps(
      leftStack = getStack(isRight),
      leftInput = leftButtonRef.current,
      rightStack = getStack(!isRight),
      rightInput = rightButtonRef.current
    )
    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      def screen = leftButtonRef.current.screen
      key.full match {
        case "tab" | "S-tab" => screen.focusNext()
        case "C-u" =>
          setIsRight(!_)
          screen.focusNext()
        case "enter" | "C-pagedown" =>
          val stack = getStack(isRightActive)
          val stackItem = stack.peek[js.Any]
          stackItem.getData.collect {
            case FileListData(_, actions, state) if actions.api.isLocal
                && FileListState.currentItem(state).exists(!_.isDir) =>
              openCurrItem(props.plugins, props.dispatch, stack, actions, state)
          }
        case keyFull =>
          props.plugins.find(_.triggerKeys.contains(keyFull)).foreach { plugin =>
            val pluginRes = plugin.onKeyTrigger(keyFull, stacks, key.data)
            pluginRes.foreach { maybePluginUi =>
              maybePluginUi.foreach { pluginUi =>
                setCurrPluginUi(Some(pluginUi))
              }
            }
            pluginRes.andThen { case Failure(_) =>
              props.dispatch(TaskAction(Task("Opening Plugin", pluginRes)))
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

    <(WithPanelStacks())(^.wrapped := stacks)(
      <.button(
        ^("isRight") := false,
        ^.reactRef := leftButtonRef,
        ^.rbMouse := true,
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbOnFocus := onActivate(isRight),
        ^.rbOnKeypress := onKeypress
      )(
        <(panelStackComp())(^.wrapped := PanelStackProps(
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
        <(panelStackComp())(^.wrapped := PanelStackProps(
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

  private def openCurrItem(plugins: Seq[FileListPlugin],
                           dispatch: Dispatch,
                           stack: PanelStack,
                           actions: FileListActions,
                           state: FileListState): Unit = {

    FileListState.currentItem(state).foreach { item =>
      def onClose: () => Unit = { () =>
        stack.pop()
      }

      val filePath = path.join(state.currDir.path, item.name)
      val openF = (for {
        source <- actions.api.readFile(state.currDir.path, item, 0.0).toFuture
        buff = new Uint8Array(64 * 1024)
        bytesRead <- source.readNextBytes(buff).toFuture
        _ <- source.close().toFuture
      } yield {
        buff.subarray(0, bytesRead)
      }).flatMap { fileHeader =>
        val zero = Future.successful(Option.empty[PanelStackItem[FileListState]])
        val pluginRes = plugins.foldLeft(zero) { (resF, plugin) =>
          resF.flatMap {
            case None => plugin.onFileTrigger(filePath, fileHeader, onClose)
            case Some(_) => resF
          }
        }
        pluginRes.map { maybePluginItem =>
          maybePluginItem.foreach { item =>
            stack.push(FSPlugin.initDispatch(dispatch, FileListStateReducer, stack, item))
          }
        }
      }

      openF.andThen {
        case Failure(_) => dispatch(TaskAction(Task("Opening File", openF)))
      }
    }
  }
}
