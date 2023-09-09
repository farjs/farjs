package farjs.fs.popups

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.FileListState
import farjs.filelist.stack.WithPanelStacks
import farjs.fs.{FSDisk, FSService}
import farjs.ui.border.SingleChars
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import farjs.ui.task.Task
import farjs.ui.{Dispatch, WithSize, WithSizeProps}
import scommons.nodejs.Process.Platform
import scommons.nodejs.process
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class DrivePopupProps(dispatch: Dispatch,
                           onChangeDir: String => Unit,
                           onClose: () => Unit,
                           showOnLeft: Boolean)

object DrivePopup extends FunctionComponent[DrivePopupProps] {

  private[popups] var platform: Platform = process.platform
  private[popups] var fsService: FSService = FSService.instance
  private[popups] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[popups] var menuPopup: UiComponent[MenuPopupProps] = MenuPopup

  protected def render(compProps: Props): ReactElement = {
    val (disks, setDisks) = useState(List.empty[FSDisk])
    val props = compProps.wrapped
    val stacks = WithPanelStacks.usePanelStacks
    val data = getData(platform, disks)

    val (panelInput, currStack, otherStack) =
      if (props.showOnLeft) (stacks.leftInput, stacks.leftStack, stacks.rightStack)
      else (stacks.rightInput, stacks.rightStack, stacks.leftStack)
    
    def onAction(dir: String): Unit = {
      val targetDir =
        otherStack.peekLast[FileListState].state.collect {
          case s if s.currDir.path.startsWith(dir) => s.currDir.path
        }.orElse(
          currStack.peekLast[FileListState].state.collect {
            case s if s.currDir.path.startsWith(dir) => s.currDir.path
          }
        ).getOrElse(dir)
      
      props.onChangeDir(targetDir)
    }

    useLayoutEffect({ () =>
      val disksF = fsService.readDisks().map { disks =>
        setDisks(disks)
      }
      props.dispatch(FileListTaskAction(Task("Reading disks", disksF)))
      ()
    }, Nil)

    if (data.isEmpty) null
    else {
      <(withSizeComp())(^.plain := WithSizeProps { (_, _) =>
        <(menuPopup())(^.wrapped := MenuPopupProps(
          title = "Drive",
          items = data.map(_._2),
          getLeft = { width =>
            val panelWidth =
              if (panelInput != null) panelInput.width
              else 0
  
            MenuPopup.getLeftPos(panelWidth, props.showOnLeft, width)
          },
          onSelect = { index =>
            onAction(data(index)._1)
          },
          onClose = props.onClose
        ))()
      })()
    }
  }

  private val kBytes = 1024d
  private val mBytes = 1024d * kBytes
  private val gBytes = 1024d * mBytes

  private[popups] def getData(platform: Platform, disks: List[FSDisk]): List[(String, String)] = {
    if (disks.isEmpty) Nil
    else {
      val items = disks.map { d =>
        (d.root, d.name, toCompact(d.size), toCompact(d.free))
      }
      val maxNameWidth = 15
      val maxSizeWidth = items.maxBy(_._3.length)._3.length
      val maxFreeWidth = items.maxBy(_._4.length)._4.length
      val sep = SingleChars.vertical

      items.map { case (root, iName, iSize, iFree) =>
        val name = iName.take(maxNameWidth).padTo(maxNameWidth, ' ')
        val size = s"${" " * (maxSizeWidth - iSize.length)}$iSize"
        val free = s"${" " * (maxFreeWidth - iFree.length)}$iFree"
        
        if (platform == Platform.win32) (root, s"  $root $sep$name$sep$size$sep$free ")
        else (root, s" $name$sep$size$sep$free ")
      }
    }
  }
  
  private[popups] def toCompact(bytes: Double): String = {
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
