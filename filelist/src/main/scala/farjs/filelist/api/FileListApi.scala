package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

trait FileListApi {

  def capabilities: Set[String]

  def readDir(parent: Option[String], dir: String): Future[FileListDir]

  def readDir(targetDir: String): Future[FileListDir]
  
  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = Future.unit

  def mkDirs(dirs: List[String]): Future[Unit] = Future.unit

  def readFile(parentDirs: List[String], item: FileListItem, position: Double): Future[FileSource] = {
    Future.successful(new FileSource {
      val file: String = item.name

      def readNextBytes(buff: Uint8Array): Future[Int] = Future.successful(0)

      def close(): Future[Unit] = Future.unit
    })
  }

  def writeFile(parentDirs: List[String],
                fileName: String,
                onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = {

    Future.successful(None)
  }
}
