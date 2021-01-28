package farjs.app.filelist

import farjs.api.filelist._
import farjs.app.filelist.FileListApiImpl._
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class FileListApiImpl extends FileListApi {

  private[filelist] def fs: FS = scommons.nodejs.fs

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = {
    readDir(path.resolve(parent.toList :+ dir: _*))
  }
  
  def readDir(targetDir: String): Future[FileListDir] = {
    fs.readdir(targetDir).map { files =>
      val items = files.map { name =>
        Try(fs.lstatSync(path.join(targetDir, name))) match {
          case Failure(_) => FileListItem(name)
          case Success(stats) =>
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

    val names = if (multiple) dir.split(path.sep.head).toList else List(dir)
    mkDirs(parent, names).map(_ => names.head)
  }
  
  private[filelist] def getPermissions(mode: Int): String = {
    
    def flag(c: Char, f: Int): Char = if ((mode & f) != 0) c else '-'
    
    val chars = new Array[Char](10)
    chars(0) = flag('d', S_IFDIR)
    chars(1) = flag('r', S_IRUSR)
    chars(2) = flag('w', S_IWUSR)
    chars(3) = flag('x', S_IXUSR)
    chars(4) = flag('r', S_IRGRP)
    chars(5) = flag('w', S_IWGRP)
    chars(6) = flag('x', S_IXGRP)
    chars(7) = flag('r', S_IROTH)
    chars(8) = flag('w', S_IWOTH)
    chars(9) = flag('x', S_IXOTH)
    
    new String(chars)
  }
}

object FileListApiImpl {

  private val S_IFDIR: Int = FSConstants.S_IFDIR
  private val S_IRUSR: Int = FSConstants.S_IRUSR.getOrElse(0)
  private val S_IWUSR: Int = FSConstants.S_IWUSR.getOrElse(0)
  private val S_IXUSR: Int = FSConstants.S_IXUSR.getOrElse(0)
  private val S_IRGRP: Int = FSConstants.S_IRGRP.getOrElse(0)
  private val S_IWGRP: Int = FSConstants.S_IWGRP.getOrElse(0)
  private val S_IXGRP: Int = FSConstants.S_IXGRP.getOrElse(0)
  private val S_IROTH: Int = FSConstants.S_IROTH.getOrElse(0)
  private val S_IWOTH: Int = FSConstants.S_IWOTH.getOrElse(0)
  private val S_IXOTH: Int = FSConstants.S_IXOTH.getOrElse(0)
}
