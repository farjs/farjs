package scommons.farc.app.filelist

import scommons.farc.api.filelist._
import scommons.nodejs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListApiImpl extends FileListApi {

  def rootDir: String = {
    path.parse(process.cwd()).root.getOrElse("")
  }
  
  def changeDir(dir: String): Future[String] = Future {
    process.chdir(path.resolve(dir))
    process.cwd()
  }
  
  def listFiles: Future[Seq[FileListItem]] = {
    val dir = process.cwd()
    
    fs.readdir(dir).map { files =>
      files.map { name =>
        val stats = fs.lstatSync(path.join(dir, name))

        val isDir = stats.isDirectory()
        FileListItem(
          name = name,
          isDir = isDir,
          isSymLink = stats.isSymbolicLink(),
          size = if (isDir) 0.0 else stats.size,
          atimeMs = stats.atimeMs,
          mtimeMs = stats.mtimeMs,
          ctimeMs = stats.ctimeMs,
          birthtimeMs = stats.birthtimeMs
        )
      }
    }
  }
}
