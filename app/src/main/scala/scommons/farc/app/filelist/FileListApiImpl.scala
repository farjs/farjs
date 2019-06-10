package scommons.farc.app.filelist

import scommons.farc.api.filelist._
import scommons.nodejs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListApiImpl extends FileListApi {

  def listFiles(dir: String): Future[Seq[FileListItem]] = fs.readdir(dir).map { files =>
    files.map { name =>
      val stats = fs.lstatSync(path.join(dir, name))

      FileListItem(
        name = name,
        isDir = stats.isDirectory(),
        isSymLink = stats.isSymbolicLink(),
        size = stats.size,
        atimeMs = stats.atimeMs,
        mtimeMs = stats.mtimeMs,
        ctimeMs = stats.ctimeMs,
        birthtimeMs = stats.birthtimeMs
      )
    }
  }
}
