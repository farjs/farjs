package farjs.filelist

import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactContext

trait FileListPlugin {

  val triggerKey: Option[String] = None
  
  def onKeyTrigger(leftStack: PanelStack, rightStack: PanelStack): Unit = ()

  def onFileTrigger(filePath: String, onClose: () => Unit): Option[PanelStackItem[_]] = None
}

object FileListPlugin {

  val Context: ReactContext[Seq[FileListPlugin]] =
    ReactContext[Seq[FileListPlugin]](defaultValue = Nil)
}
