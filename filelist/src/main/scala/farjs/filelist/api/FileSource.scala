package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

trait FileSource extends js.Object {
  
  val file: String
  
  def readNextBytes(buff: Uint8Array): js.Promise[Int]

  def close(): js.Promise[Unit]
}
