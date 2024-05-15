package farjs.file

import farjs.filelist._
import farjs.filelist.stack._
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

object FilePlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("M-v")

  override def onKeyTrigger(key: String,
                            stacks: PanelStacks,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Future[Option[ReactClass]] = {

    Future.successful(createUi(key).map(_.apply()))
  }
  
  private[file] def createUi(key: String): Option[FilePluginUi] = {
    key match {
      case "M-v" => Some(new FilePluginUi(showFileViewHistoryPopup = true))
      case _ => None
    }
  }
}
