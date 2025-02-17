package farjs.file

import farjs.filelist._
import farjs.filelist.stack._
import scommons.react.ReactClass

import scala.scalajs.js

object FilePlugin extends FileListPlugin(js.Array("M-v")) {

  override def onKeyTrigger(key: String,
                            stacks: WithStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = {

    js.Promise.resolve[js.UndefOr[ReactClass]](createUi(key).map(FilePluginUi(_)))
  }
  
  private[file] final def createUi(key: String): js.UndefOr[FilePluginUiParams] = {
    key match {
      case "M-v" => FilePluginUiParams(showFileViewHistoryPopup = true)
      case _ => js.undefined
    }
  }
}
