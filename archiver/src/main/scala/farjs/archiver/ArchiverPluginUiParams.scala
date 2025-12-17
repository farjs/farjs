package farjs.archiver

import farjs.filelist.FileListData
import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait ArchiverPluginUiParams extends js.Object {
  val data: FileListData
  val archName: String
  val archType: String
  val addToArchApi: js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]]
  val items: js.Array[FileListItem]
}

object ArchiverPluginUiParams {

  def apply(data: FileListData,
            archName: String,
            archType: String,
            addToArchApi: js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]],
            items: js.Array[FileListItem]): ArchiverPluginUiParams = {

    js.Dynamic.literal(
      data = data,
      archName = archName,
      archType = archType,
      addToArchApi = addToArchApi,
      items = items
    ).asInstanceOf[ArchiverPluginUiParams]
  }

  def unapply(arg: ArchiverPluginUiParams): Option[
    (FileListData, String, String, js.Function4[String, String, js.Set[String], js.Function0[Unit], js.Promise[Unit]], js.Array[FileListItem])
  ] = {
    Some((
      arg.data,
      arg.archName,
      arg.archType,
      arg.addToArchApi,
      arg.items
    ))
  }
}
