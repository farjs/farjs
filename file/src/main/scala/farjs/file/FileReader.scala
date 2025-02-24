package farjs.file

import scommons.nodejs.Buffer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../file/FileReader.mjs", JSImport.Default)
class FileReader extends js.Object {
  
  def open(filePath: String): js.Promise[Unit] = js.native

  def close(): js.Promise[Unit] = js.native

  def readBytes(position: Double, buf: Buffer): js.Promise[Int] = js.native
}
