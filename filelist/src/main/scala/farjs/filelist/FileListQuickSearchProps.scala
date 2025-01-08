package farjs.filelist

import scala.scalajs.js

sealed trait FileListQuickSearchProps extends js.Object {
  val text: String
  val onClose: js.Function0[Unit]
}

object FileListQuickSearchProps {

  def apply(text: String, onClose: js.Function0[Unit]): FileListQuickSearchProps = {
    js.Dynamic.literal(
      text = text,
      onClose = onClose
    ).asInstanceOf[FileListQuickSearchProps]
  }

  def unapply(arg: FileListQuickSearchProps): Option[(String, js.Function0[Unit])] = {
    Some((
      arg.text,
      arg.onClose
    ))
  }
}
