package farjs.filelist.fs

import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.nodejs.Process.Platform
import scommons.nodejs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FSService(platform: Platform, childProcess: ChildProcess) {

  def openItem(parent: String, item: String): Future[Unit] = {
    val name =
      if (item == FileListItem.up.name) FileListDir.curr
      else item

    val (_, future) = childProcess.exec(
      command = {
        if (platform == Platform.darwin) s"""open "$name""""
        else if (platform == Platform.win32) s"""start "" "$name""""
        else s"""xdg-open "$name""""
      },
      options = Some(new raw.ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })
    )

    future.map(_ => ())
  }

  def readDisk(path: String): Future[Option[FSDisk]] = {
    ???
  }
}
