package farjs.app.filelist.fs

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.FileListState
import farjs.filelist.stack.{PanelStack, WithPanelStacks}
import farjs.ui.border.SingleBorder
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import scommons.nodejs.Process.Platform
import scommons.nodejs.process
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global

case class FSDrivePopupProps(dispatch: Dispatch,
                             onClose: () => Unit,
                             showOnLeft: Boolean)

object FSDrivePopup extends FunctionComponent[FSDrivePopupProps] {

  private[fs] var platform: Platform = process.platform
  private[fs] var fsService: FSService = FSService.instance
  private[fs] var menuPopup: UiComponent[MenuPopupProps] = MenuPopup

  protected def render(compProps: Props): ReactElement = {
    val (disks, setDisks) = useState(List.empty[FSDisk])
    val props = compProps.wrapped
    val stacks = WithPanelStacks.usePanelStacks
    val stackProps = PanelStack.usePanelStack
    val data = getData(platform, disks)

    def onAction(dir: String): Unit = {
      props.onClose()
      
      val (currStack, otherStack) =
        if (stackProps.stack == stacks.leftStack) (stackProps.stack, stacks.rightStack)
        else (stackProps.stack, stacks.leftStack)
      
      if (currStack.peek != currStack.peekLast) {
        currStack.clear()
      }
      val targetDir =
        otherStack.peekLast[FileListState].state.collect {
          case s if s.currDir.path.startsWith(dir) => s.currDir.path
        }.orElse(
          currStack.peekLast[FileListState].state.collect {
            case s if s.currDir.path.startsWith(dir) => s.currDir.path
          }
        ).getOrElse(dir)
      
      currStack.peekLast[FileListState].getActions.foreach { case (dispatch, actions) =>
        dispatch(actions.changeDir(
          dispatch = dispatch,
          parent = None,
          dir = targetDir
        ))
      }
    }

    useLayoutEffect({ () =>
      val disksF = fsService.readDisks().map { disks =>
        setDisks(disks)
      }
      props.dispatch(FileListTaskAction(FutureTask("Reading disks", disksF)))
      ()
    }, Nil)

    if (data.isEmpty) null
    else {
      <(menuPopup())(^.wrapped := MenuPopupProps(
        title = "Drive",
        items = data.map(_._2),
        getLeft = { width =>
          MenuPopup.getLeftPos(stackProps.width, props.showOnLeft, width)
        },
        onSelect = { index =>
          onAction(data(index)._1)
        },
        onClose = props.onClose
      ))()
    }
  }

  private val kBytes = 1024d
  private val mBytes = 1024d * kBytes
  private val gBytes = 1024d * mBytes

  private[fs] def getData(platform: Platform, disks: List[FSDisk]): List[(String, String)] = {
    if (disks.isEmpty) Nil
    else {
      val items = disks.map { d =>
        (d.root, d.name, toCompact(d.size), toCompact(d.free))
      }
      val maxNameWidth = 15
      val maxSizeWidth = items.maxBy(_._3.length)._3.length
      val maxFreeWidth = items.maxBy(_._4.length)._4.length
      val sep = SingleBorder.verticalCh

      items.map { case (root, iName, iSize, iFree) =>
        val name = iName.take(maxNameWidth).padTo(maxNameWidth, ' ')
        val size = s"${" " * (maxSizeWidth - iSize.length)}$iSize"
        val free = s"${" " * (maxFreeWidth - iFree.length)}$iFree"
        
        if (platform == Platform.win32) (root, s"  $root $sep$name$sep$size$sep$free ")
        else (root, s" $name$sep$size$sep$free ")
      }
    }
  }
  
  private[fs] def toCompact(bytes: Double): String = {
    if (bytes == 0.0) ""
    else {
      val (size, mod) =
        if (bytes > 1000d * gBytes) (bytes / gBytes, " G")
        else if (bytes > 1000d * mBytes) (bytes / mBytes, " M")
        else if (bytes > 1000d * kBytes) (bytes / kBytes, " K")
        else (bytes, "")
  
      f"$size%.0f$mod"
    }
  }
}
