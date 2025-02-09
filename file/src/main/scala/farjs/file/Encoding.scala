package farjs.file

import scommons.nodejs.Buffer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../file/Encoding.mjs", JSImport.Default)
object Encoding extends js.Object {
  
  val encodings: js.Array[String] = js.native
  
  val platformEncoding: String = js.native

  def decode(buf: Buffer, encoding: String, start: Int, end: Int): String = js.native

  def byteLength(string: String, encoding: String): Int = js.native
}
