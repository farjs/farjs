package farjs.archiver.zip

import farjs.filelist.api._
import farjs.filelist.util.{ChildProcess, StreamReader, SubProcess}
import scommons.nodejs.{Buffer, raw}

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8Array

class ZipApi(
  val zipPath: String,
  val rootPath: String,
  private var entriesByParentF: Future[Map[String, List[ZipEntry]]]
) extends FileListApi(false, js.Set(
  FileListCapability.read,
  FileListCapability.delete
)) {

  override def readDir(parent: String, maybeDir: js.UndefOr[String]): js.Promise[FileListDir] = {
    val path = if (parent == "") rootPath else parent
    val targetDir = maybeDir.map { dir =>
      if (dir == FileListItem.up.name) {
        val lastSlash = path.lastIndexOf('/')
        path.take(lastSlash)
      }
      else if (dir == FileListItem.currDir.name) path
      else s"$path/$dir"
    }.getOrElse(path)
    
    entriesByParentF.map { entriesByParent =>
      val path = targetDir.stripPrefix(rootPath).stripPrefix("/")
      val entries = entriesByParent.getOrElse(path, Nil)

      FileListDir(
        path = targetDir,
        isRoot = false,
        items = js.Array(entries.map(ZipApi.convertToFileListItem): _*)
      )
    }.toJSPromise
  }

  override def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] = {

    def deleteFromState(parent: String, items: js.Array[FileListItem]): Unit = {
      entriesByParentF = entriesByParentF.map { entriesByParent =>
        items.foldLeft(entriesByParent) { (entries, item) =>
          val res = entries.updatedWith(parent.stripPrefix(rootPath).stripPrefix("/")) {
            _.map(_.filter(_.name != item.name))
          }
          if (item.isDir) {
            res.removed(s"$parent/${item.name}".stripPrefix(rootPath).stripPrefix("/"))
          }
          else res
        }
      }
    }
    
    def delDirItems(parent: String, items: js.Array[FileListItem]): Future[Unit] = {
      items.foldLeft(Future.successful(())) { case (res, item) =>
        res.flatMap { _ =>
          if (item.isDir) {
            val dir = s"$parent/${item.name}"
            readDir(dir, js.undefined).toFuture.flatMap { fileListDir =>
              if (fileListDir.items.nonEmpty) {
                delDirItems(dir, fileListDir.items)
              }
              else Future.successful {
                deleteFromState(parent, js.Array(item))
              }
            }
          }
          else Future.successful(())
        }
      }.flatMap { _ =>
        val paths = items.map { item =>
          val name = if (item.isDir) s"${item.name}/" else item.name
          s"$parent/$name".stripPrefix(rootPath).stripPrefix("/")
        }
  
        val (_, future) = ZipApi.childProcess.exec(
          command = s"""zip -qd "$zipPath" ${paths.mkString("\"", "\" \"", "\"")}""",
          options = Some(new raw.ChildProcessOptions {
            override val windowsHide = true
          })
        )

        deleteFromState(parent, items)
        future.map(_ => ())
      }
    }

    delDirItems(parent, items).toJSPromise
  }

  override def readFile(parent: String, item: FileListItem, position: Double): js.Promise[FileSource] = {
    val filePath = s"$parent/${item.name}".stripPrefix(rootPath).stripPrefix("/")
    val subprocessF = extract(zipPath, filePath)

    subprocessF.map { case SubProcess(_, stdout, exitP) =>
      new FileSource {
        private var pos = 0

        override val file: String = filePath

        override def readNextBytes(buff: Uint8Array): js.Promise[Int] = {
          stdout.readNextBytes(buff.length).toFuture.map(_.toOption).flatMap {
            case None =>
              if (pos != item.size) {
                exitP.toFuture.map { maybeError =>
                  maybeError.toOption match {
                    case None => 0
                    case Some(error) => throw js.JavaScriptException(error)
                  }
                }
              }
              else Future.successful(0)
            case Some(content) => Future.successful {
              val bytesRead = content.length
              for (i <- 0 until bytesRead) {
                buff(i) = content(i)
              }

              pos += bytesRead
              bytesRead
            }
          }.toJSPromise
        }

        override def close(): js.Promise[Unit] = {
          if (pos != item.size) {
            stdout.readable.destroy(js.undefined)
          }

          exitP.`then`[Unit](_ => ())
        }
      }
    }.toJSPromise
  }

  def extract(zipPath: String, filePath: String): Future[SubProcess] = {
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

  private[zip] var childProcess: ChildProcess = ChildProcess.child_process
  
  def convertToFileListItem(zip: ZipEntry): FileListItem = {
    FileListItem.copy(FileListItem(
      name = zip.name,
      isDir = zip.isDir
    ))(
      size = zip.size,
      mtimeMs = zip.datetimeMs,
      permissions = zip.permissions
    )
  }

  def addToZip(zipFile: String, parent: String, items: js.Set[String], onNextItem: js.Function0[Unit]): js.Promise[Unit] = {
    val resF = for {
      subprocess <- childProcess.spawn(
        command = "zip",
        args = List("-r", zipFile) ++ items.toList,
        options = Some(new raw.ChildProcessOptions {
          override val cwd = parent
          override val windowsHide = true
        })
      )
      _ <- subprocess.stdout.readAllLines { line =>
        if (line.contains("adding: ")) {
          onNextItem()
        }
      }.toFuture
      _ <- subprocess.exitP.toFuture
    } yield ()

    resF.toJSPromise
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
      reader.readNextBytes(64 * 1024).toFuture.map(_.toOption).flatMap {
        case None => Future.successful(result)
        case Some(content) =>
          result.push(content)
          loop(reader, result)
      }
    }

    for {
      subprocess <- subprocessF
      chunks <- loop(subprocess.stdout, new js.Array[Buffer](0))
      output = Buffer.concat(chunks).toString
      _ <- subprocess.exitP.toFuture.map { maybeError =>
        maybeError.toOption match {
          case None =>
          case Some(error) =>
            if (!error.toString.contains("code=1") || !output.contains("Empty zipfile.")) {
              throw new RuntimeException(error.toString)
            }
        }
      }
    } yield {
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
