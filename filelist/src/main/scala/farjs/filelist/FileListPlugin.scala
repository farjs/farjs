package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, PanelStacks}
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

trait FileListPlugin {

  val triggerKeys: js.Array[String] = js.Array()
  
  def onKeyTrigger(key: String,
                   stacks: PanelStacks,
                   data: js.UndefOr[js.Dynamic]): Future[Option[ReactClass]] = {
    Future.successful(None)
  }

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: () => Unit): Future[Option[PanelStackItem[FileListState]]] = {
    Future.successful(None)
  }
}
