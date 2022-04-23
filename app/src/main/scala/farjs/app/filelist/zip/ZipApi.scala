package farjs.app.filelist.zip

import farjs.filelist.api._
import scommons.nodejs.util.{StreamReader, SubProcess}
import scommons.nodejs.{Buffer, ChildProcess, child_process, raw}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.control.NonFatal

class ZipApi(zipPath: String,
             rootPath: String,
             entriesByParentF: Future[Map[String, List[ZipEntry]]]) extends FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = {
    val path = parent.getOrElse(rootPath)
    val targetDir =
      if (dir == FileListItem.up.name) {
        val lastSlash = path.lastIndexOf('/')
        path.take(lastSlash)
      }
      else if (dir == FileListItem.currDir.name) path
      else s"$path/$dir"
    
    readDir(targetDir)
  }

  def readDir(targetDir: String): Future[FileListDir] = {
    entriesByParentF.map { entriesByParent =>
      val path = targetDir.stripPrefix(rootPath).stripPrefix("/")
      val entries = entriesByParent.getOrElse(path, Nil)

      FileListDir(
        path = targetDir,
        isRoot = false,
        items = entries.map(ZipApi.convertToFileListItem)
      )
    }
  }

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = ???

  def mkDirs(dirs: List[String]): Future[Unit] = Future.unit

  def readFile(parentDirs: List[String], item: FileListItem, position: Double): Future[FileSource] = {
    val filePath = s"${parentDirs.mkString("/")}/${item.name}".stripPrefix(rootPath).stripPrefix("/")
    val subprocessF = extract(zipPath, filePath)

    subprocessF.map { case SubProcess(_, stdout, exitF) =>
      new FileSource {
        private var pos = 0

        val file: String = filePath

        def readNextBytes(buff: Uint8Array): Future[Int] = {
          stdout.readNextBytes(buff.length).flatMap {
            case None =>
              if (pos != item.size) exitF.map(_ => 0)
              else Future.successful(0)
            case Some(content) => Future.successful {
              val bytesRead = content.length
              for (i <- 0 until bytesRead) {
                buff(i) = content(i)
              }

              pos += bytesRead
              bytesRead
            }
          }
        }

        def close(): Future[Unit] = {
          if (pos != item.size) {
            stdout.readable.destroy()
          }

          exitF.recover {
            case NonFatal(_) => ()
          }
        }
      }
    }
  }

  def writeFile(parentDirs: List[String], fileName: String, onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = ???

  private[zip] def extract(zipPath: String, filePath: String): Future[SubProcess] = {
    ZipApi.childProcess.spawn(
      command = "unzip",
      args = List("-p", zipPath, filePath),
      options = Some(new raw.ChildProcessOptions {
        override val windowsHide = true
      })
    )
  }
}

object ZipApi {

  private[zip] var childProcess: ChildProcess = child_process
  
  def convertToFileListItem(zip: ZipEntry): FileListItem = {
    FileListItem(
      name = zip.name,
      isDir = zip.isDir,
      size = zip.size,
      mtimeMs = zip.datetimeMs,
      permissions = zip.permissions
    )
  }

  def addToZip(zipFile: String, parent: String, items: Set[String]): Future[Unit] = {
    val (_, future) = childProcess.exec(
      command = s"""zip -qr "$zipFile" ${items.mkString("\"", "\" \"", "\"")}""",
      options = Some(new raw.ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })
    )

    future.map(_ => ())
  }

  def readZip(zipPath: String): Future[Map[String, List[ZipEntry]]] = {
    val subprocessF = childProcess.spawn(
      command = "unzip",
      args = List("-ZT", zipPath),
      options = Some(new raw.ChildProcessOptions {
        override val windowsHide = true
      })
    )

    def loop(reader: StreamReader, result: js.Array[Buffer]): Future[js.Array[Buffer]] = {
      reader.readNextBytes(64 * 1024).flatMap {
        case None => Future.successful(result)
        case Some(content) =>
          result.push(content)
          loop(reader, result)
      }
    }

    for {
      subprocess <- subprocessF
      chunks <- loop(subprocess.stdout, new js.Array[Buffer](0))
      _ <- subprocess.exitF
    } yield {
      val output = Buffer.concat(chunks).toString
      ZipApi.groupByParent(ZipEntry.fromUnzipCommand(output))
    }
  }
  
  private[zip] def groupByParent(entries: List[ZipEntry]): Map[String, List[ZipEntry]] = {
    val processedDirs = mutable.Set[String]()
    
    @annotation.tailrec
    def ensureDirs(entry: ZipEntry, entriesByParent: Map[String, List[ZipEntry]]): Map[String, List[ZipEntry]] = {
      val values = entriesByParent.getOrElse(entry.parent, Nil)
      if (entry.name == "" || values.exists(_.name == entry.name)) entriesByParent
      else {
        val updatedEntries = entriesByParent.updatedWith(entry.parent) {
          case None => Some(entry :: Nil)
          case Some(values) => Some(entry :: values)
        }

        if (processedDirs.contains(entry.parent)) updatedEntries
        else {
          processedDirs += entry.parent
          val (parent, name) = {
            val lastSlash = entry.parent.lastIndexOf('/')
            if (lastSlash != -1) {
              (entry.parent.take(lastSlash), entry.parent.drop(lastSlash + 1))
            }
            else ("", entry.parent)
          }
          ensureDirs(
            entry = ZipEntry(
              parent = parent,
              name = name,
              isDir = true,
              datetimeMs = entry.datetimeMs,
              permissions = "drw-r--r--"
            ),
            entriesByParent = updatedEntries
          )
        }
      }
    }
    
    var entriesByParent = Map.empty[String, List[ZipEntry]]
    entries.foreach { entry =>
      entriesByParent = ensureDirs(entry, entriesByParent)
    }

    entriesByParent
  }
}
