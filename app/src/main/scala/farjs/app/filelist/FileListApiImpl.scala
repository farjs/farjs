package farjs.app.filelist

import farjs.api.filelist._
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileListApiImpl extends FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = {
    readDir(path.resolve(parent.toList :+ dir: _*))
  }
  
  def readDir(targetDir: String): Future[FileListDir] = {
    fs.readdir(targetDir).map { files =>
      val items = files.map { name =>
        val stats = fs.lstatSync(path.join(targetDir, name))

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

      val pathObj = path.parse(targetDir)
      FileListDir(
        path = targetDir,
        isRoot = pathObj.root == pathObj.dir && pathObj.base.getOrElse("").isEmpty,
        items = items
      )
    }
  }

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = {
    
    def delDirItems(parent: String, items: Seq[(String, Boolean)]): Future[Unit] = {
      items.foldLeft(Future.successful(())) { case (res, (name, isDir)) =>
        res.flatMap { _ =>
          if (isDir) {
            val dir = path.join(parent, name)
            fs.readdir(dir).flatMap { files =>
              val items = files.map { name =>
                val stats = fs.lstatSync(path.join(dir, name))
                (name, stats.isDirectory())
              }
              delDirItems(dir, items).map { _ =>
                fs.rmdirSync(dir)
              }
            }
          }
          else Future.successful {
            fs.unlinkSync(path.join(parent, name))
          }
        }
      }
    }

    delDirItems(parent, items.map(i => (i.name, i.isDir)))
  }

  def mkDir(parent: String, dir: String, multiple: Boolean): Future[String] = {

    def mkDirs(parent: String, names: List[String]): Future[Unit] = names match {
      case Nil => Future.unit
      case name :: tail => Future.unit.flatMap { _ =>
        val dir =
          if (name.isEmpty) parent
          else {
            val dir = path.join(parent, name)
            if (!fs.existsSync(dir)) {
              fs.mkdirSync(dir)
            }
            dir
          }
        
        mkDirs(dir, tail)
      }
    }

    val names = if (multiple) dir.split(path.sep).toList else List(dir)
    mkDirs(parent, names).map(_ => names.head)
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
