package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

trait FileTarget extends js.Object {

  val file: String
  
  def writeNextBytes(buff: Uint8Array, length: Int): js.Promise[Double]
  
  def setAttributes(src: FileListItem): js.Promise[Unit]

  def close(): js.Promise[Unit]

  def delete(): js.Promise[Unit]
}
