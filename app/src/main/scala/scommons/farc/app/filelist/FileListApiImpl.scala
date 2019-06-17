package scommons.farc.app.filelist

import scommons.farc.api.filelist._
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants._

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
          birthtimeMs = stats.birthtimeMs,
          permissions = getPermissions(stats.mode)
        )
      }
    }
  }
  
  private[filelist] def getPermissions(mode: Int): String = {
    
    def flag(c: Char, f: Int): Char = if ((mode & f) != 0) c else '-'
    
    s"${flag('d', S_IFDIR)}" +
      s"${flag('r', S_IRUSR)}" +
      s"${flag('w', S_IWUSR)}" +
      s"${flag('x', S_IXUSR)}" +
      s"${flag('r', S_IRGRP)}" +
      s"${flag('w', S_IWGRP)}" +
      s"${flag('x', S_IXGRP)}" +
      s"${flag('r', S_IROTH)}" +
      s"${flag('w', S_IWOTH)}" +
      s"${flag('x', S_IXOTH)}"
  }
}
