package farjs.archiver.zip

import farjs.filelist.api._
import farjs.filelist.util.{ChildProcess, SubProcess}

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
