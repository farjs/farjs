package farjs.archiver.zip

import farjs.filelist.api._
import farjs.filelist.util.{ChildProcess, SubProcess}
import scommons.nodejs.raw

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.Uint8Array

class ZipApi(
  zipPathIn: String,
  rootPathIn: String,
  entriesByParentIn: js.Promise[js.Map[String, js.Array[FileListItem]]]
) extends ZipApiNative(zipPathIn, rootPathIn, entriesByParentIn) {

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
    
    entriesByParentP.toFuture.map { entriesByParent =>
      val path = targetDir.stripPrefix(rootPath).stripPrefix("/")
      val entries = entriesByParent.getOrElse(path, js.Array())

      FileListDir(
        path = targetDir,
        isRoot = false,
        items = entries
      )
    }.toJSPromise
  }

  override def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] = {

    def deleteFromState(parent: String, items: js.Array[FileListItem]): Unit = {
      entriesByParentP = entriesByParentP.toFuture.map { entriesByParent =>
        items.foldLeft(new js.Map[String, js.Array[FileListItem]](entriesByParent)) { (entries, item) =>
          entries.updateWith(parent.stripPrefix(rootPath).stripPrefix("/")) {
            _.map(_.filter(_.name != item.name))
          }
          if (item.isDir) {
            entries.delete(s"$parent/${item.name}".stripPrefix(rootPath).stripPrefix("/"))
          }
          entries
        }
      }.toJSPromise
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
  
        val future = ZipApi.childProcess.spawn(
          command = "zip",
          args = List("-qd", zipPath) ++ paths.toList,
          options = Some(new raw.ChildProcessOptions {
            override val windowsHide = true
          })
        )

        deleteFromState(parent, items)
        for {
          s <- future
          _ = s.stdout.readable.destroy()
          _ <- s.exitP.toFuture.map { maybeError =>
            maybeError.toOption match {
              case None =>
              case Some(error) => throw js.JavaScriptException(error)
            }
          }
        } yield ()
      }
    }

    delDirItems(parent, items).toJSPromise
  }

  override def readFile(parent: String, item: FileListItem, position: Double): js.Promise[FileSource] = {
    val filePath = s"$parent/${item.name}".stripPrefix(rootPath).stripPrefix("/")
    val subprocessF = extract(zipPath, filePath)

    subprocessF.toFuture.map { case SubProcess(_, stdout, exitP) =>
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

          exitP.toFuture.map(_ => ()).toJSPromise
        }
      }
    }.toJSPromise
  }
}

object ZipApi {

  private[zip] var childProcess: ChildProcess = ChildProcess.child_process
  
  def addToZip(zipFile: String, parent: String, items: js.Set[String], onNextItem: js.Function0[Unit]): js.Promise[Unit] =
    ZipApiNative.addToZip(zipFile, parent, items, onNextItem)

  def readZip(zipPath: String): js.Promise[js.Map[String, js.Array[FileListItem]]] =
    ZipApiNative.readZip(zipPath)
}

@js.native
@JSImport("../archiver/zip/ZipApi.mjs", JSImport.Default)
class ZipApiNative(
                    val zipPath: String,
                    val rootPath: String,
                    protected var entriesByParentP: js.Promise[js.Map[String, js.Array[FileListItem]]]
                  ) extends FileListApi(js.native, js.native) {

  def extract(zipPath: String, filePath: String): js.Promise[SubProcess] = js.native
}

@js.native
@JSImport("../archiver/zip/ZipApi.mjs", JSImport.Default)
object ZipApiNative extends js.Object {

  def addToZip(zipFile: String, parent: String, items: js.Set[String], onNextItem: js.Function0[Unit]): js.Promise[Unit] = js.native
  
  def readZip(zipPath: String): js.Promise[js.Map[String, js.Array[FileListItem]]] = js.native
}
