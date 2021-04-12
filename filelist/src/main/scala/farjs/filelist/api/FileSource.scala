package farjs.filelist.api

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

trait FileSource {
  
  def file: String
  
  def readNextBytes(buff: Uint8Array): Future[Int]

  def close(): Future[Unit]
}
