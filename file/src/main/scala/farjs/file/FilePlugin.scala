package farjs.file

import farjs.filelist._
import farjs.filelist.stack._
import scommons.react.ReactClass

import scala.scalajs.js

object FilePlugin extends FileListPlugin(js.Array("M-v")) {

  override def onKeyTrigger(key: String,
                            stacks: PanelStacks,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = {

    js.Promise.resolve[js.UndefOr[ReactClass]](createUi(key).map(_.apply()) match {
      case Some(r) => r
      case None => js.undefined
    })
  }
  
  private[file] final def createUi(key: String): Option[FilePluginUi] = {
    key match {
      case "M-v" => Some(new FilePluginUi(showFileViewHistoryPopup = true))
      case _ => None
    }
  }
}
