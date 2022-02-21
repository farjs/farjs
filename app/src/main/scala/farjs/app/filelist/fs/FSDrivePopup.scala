package farjs.app.filelist.fs

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.FileListState
import farjs.filelist.stack.PanelStack
import farjs.ui._
import farjs.ui.border.SingleBorder
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs.Process.Platform
import scommons.nodejs.process
import scommons.react._
import scommons.react.blessed._
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

  private[fs] var popupComp: UiComponent[PopupProps] = Popup
  private[fs] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[fs] var buttonComp: UiComponent[ButtonProps] = Button

  protected def render(compProps: Props): ReactElement = {
    val (disks, setDisks) = useState(List.empty[FSDisk])
    val props = compProps.wrapped
    val stackProps = PanelStack.usePanelStack

    val data = getData(platform, disks)
    val textWidth = if (data.isEmpty) 10 else data.maxBy(_._2.length)._2.length
    val width = textWidth + (paddingHorizontal + 1) * 2
    val height = (paddingVertical + 1) * 2 + data.size
    val theme = Theme.current.popup.menu

    def onAction(dir: String): () => Unit = { () =>
      props.onClose()
      
      val stack = stackProps.stack
      if (stack.peek != stack.peekLast) {
        stack.pop()
      }
      stack.peekLast[FileListState].getActions.foreach { case (dispatch, actions) =>
        dispatch(actions.changeDir(
          dispatch = dispatch,
          parent = None,
          dir = dir
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
      <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
        <(modalContentComp())(^.wrapped := ModalContentProps(
          title = "Drive",
          size = (width, height),
          style = theme,
          padding = padding,
          left = getLeftPos(stackProps.width, props.showOnLeft, width)
        ))(
          data.zipWithIndex.map { case ((root, text), index) =>
            <(buttonComp())(^.key := s"$index", ^.wrapped := ButtonProps(
              pos = (1, 1 + index),
              label = text,
              style = theme,
              onPress = onAction(root)
            ))()
          }
        )
      )
    }
  }

  private val paddingHorizontal = 2
  private val paddingVertical = 1
  private[fs] val padding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
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
  
  private[fs] def getLeftPos(stackWidth: Int, showOnLeft: Boolean, width: Int): String = {
    val pos =
      if (width <= stackWidth) (stackWidth - width) / 2
      else stackWidth - width
    val (left, normalizedPos) =
      if (showOnLeft || width > stackWidth * 2) ("0%", math.max(pos, 0))
      else ("50%", pos)

    if (normalizedPos >= 0) s"$left+$normalizedPos"
    else s"$left$normalizedPos"
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
