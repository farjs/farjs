package farjs.filelist

import scala.scalajs.js

sealed trait FileListPluginUiProps extends js.Object {
  
  def onClose: js.Function0[Unit]
}

object FileListPluginUiProps {

  def apply(onClose: js.Function0[Unit]): FileListPluginUiProps = {
    js.Dynamic.literal(
      onClose = onClose
    ).asInstanceOf[FileListPluginUiProps]
  }

  def unapply(arg: FileListPluginUiProps): Option[js.Function0[Unit]] = {
    Some(
      arg.onClose
    )
  }
}
