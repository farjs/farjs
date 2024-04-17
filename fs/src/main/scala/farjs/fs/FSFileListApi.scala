package farjs.fs

import farjs.filelist.api._
import farjs.fs.FSFileListApi._
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.{Failure, Success, Try}

class FSFileListApi(fs: FS = scommons.nodejs.fs) extends FileListApi {

  override val capabilities: js.Set[FileListCapability] = js.Set[FileListCapability](
    FileListCapability.read,
    FileListCapability.write,
    FileListCapability.delete,
    FileListCapability.mkDirs,
    FileListCapability.copyInplace,
    FileListCapability.moveInplace
  )

  override def readDir(parent: String, dir: js.UndefOr[String]): js.Promise[FileListDir] = {
    val targetDir = path.resolve(parent, dir.getOrElse(""))
  
    fs.readdir(targetDir).map { files =>
      val items = files.map { name =>
        toFileListItem(targetDir, name)
      }

      FileListDir(
        path = targetDir,
        isRoot = isRoot(targetDir),
        items = js.Array(items: _*)
      )
    }.toJSPromise
  }

  override def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] = {
    
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

    delDirItems(parent, items.map(i => (i.name, i.isDir)).toSeq).toJSPromise
  }

  override def mkDirs(dirs: js.Array[String]): js.Promise[String] = {

    def loop(parent: String, names: List[String]): Future[String] = names match {
      case Nil => Future.successful(parent)
      case name :: tail => Future.unit.flatMap { _ =>
        val dir =
          if (name.isEmpty) parent
          else {
            val dir = path.join(parent, name)
            if (parent.nonEmpty || !isRoot(dir)) {
              try {
                fs.mkdirSync(dir)
              }
              catch {
                case JavaScriptException(error: raw.Error) if error.code == "EEXIST" => //skip
              }
            }
            dir
          }
        
        loop(dir, tail)
      }
    }

    loop("", dirs.toList).toJSPromise
  }
  
  override def readFile(parent: String, file: FileListItem, position: Double): js.Promise[FileSource] = Future {
    val filePath = path.join(parent, file.name)
    val fd = fs.openSync(filePath, FSConstants.O_RDONLY)
    
    new FileSource {
      private var pos = position

      override val file: String = filePath

      override def readNextBytes(buff: Uint8Array): js.Promise[Int] = {
        fs.read(fd, buff, 0, buff.length, pos).map { bytesRead =>
          pos += bytesRead
          bytesRead
        }.toJSPromise
      }

      override def close(): js.Promise[Unit] = Future(fs.closeSync(fd)).toJSPromise
    }
  }.toJSPromise

  override def writeFile(parent: String,
                         fileName: String,
                         onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]
                        ): js.Promise[js.UndefOr[FileTarget]] = {

    val filePath = path.join(parent, fileName)

    Future[(js.UndefOr[Int], Double)] {
      val fd = fs.openSync(filePath, FSConstants.O_CREAT | FSConstants.O_WRONLY | FSConstants.O_EXCL)
      (fd, 0.0)
    }.recoverWith[(js.UndefOr[Int], Double)] {
      case JavaScriptException(error: raw.Error) if error.code == "EEXIST" =>
        val existing = toFileListItem(parent, fileName)
        onExists(existing).toFuture.map(_.toOption match {
          case None => (js.undefined, 0.0)
          case Some(overwrite) =>
            val fd = fs.openSync(filePath, FSConstants.O_WRONLY)
            val position = if (overwrite) 0.0 else existing.size
            (fd, position)
        })
    }.map { case (maybeFd, position) =>
      maybeFd.map { fd =>
        new FileTarget {
          private var pos = position
          
          override val file: String = filePath

          override def writeNextBytes(buff: Uint8Array, length: Int): js.Promise[Double] = {
            fs.write(fd, buff, 0, length, pos).map { bytesWritten =>
              if (bytesWritten != length) {
                throw new IllegalStateException(
                  s"Error: bytesWritten($bytesWritten) != expected($length), file: $file"
                )
              }
              pos += bytesWritten
              pos
            }.toJSPromise
          }

          override def setAttributes(src: FileListItem): js.Promise[Unit] = {
            fs.ftruncate(fd, pos).map { _ =>
              fs.futimesSync(fd, src.atimeMs / 1000, src.mtimeMs / 1000)
            }.toJSPromise
          }

          override def close(): js.Promise[Unit] = Future(fs.closeSync(fd)).toJSPromise

          override def delete(): js.Promise[Unit] = Future(fs.unlinkSync(file)).toJSPromise
        }
      }
    }
  }.toJSPromise

  private def toFileListItem(targetDir: String, name: String): FileListItem = {
    Try(fs.lstatSync(path.join(targetDir, name))) match {
      case Failure(_) => FileListItem(name)
      case Success(stats) =>
        val isDir = stats.isDirectory()
        FileListItem.copy(FileListItem(name, isDir))(
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

object FSFileListApi {
  
  private def isRoot(dir: String): Boolean = {
    val pathObj = path.parse(dir)

    pathObj.root == pathObj.dir &&
      pathObj.base.getOrElse("").isEmpty
  }

  private[fs] def getPermissions(mode: Int): String = {
    
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
