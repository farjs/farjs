package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

trait FileTarget {

  def file: String
  
  def writeNextBytes(buff: Uint8Array, length: Int): Future[Double]
  
  def setAttributes(src: FileListItem): Future[Unit]

  def close(): Future[Unit]

  def delete(): Future[Unit]
}
