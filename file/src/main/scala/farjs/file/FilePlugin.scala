package farjs.file

import farjs.filelist._
import farjs.filelist.stack._
import scommons.react.ReactClass

import scala.scalajs.js

object FilePlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("M-v")

  override def onKeyTrigger(key: String,
                            stacks: WithPanelStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Option[ReactClass] = {

    createUi(key).map(_.apply())
  }
  
  private[file] def createUi(key: String): Option[FilePluginUi] = {
    key match {
      case "M-v" => Some(new FilePluginUi(showFileViewHistoryPopup = true))
      case _ => None
    }
  }
}
