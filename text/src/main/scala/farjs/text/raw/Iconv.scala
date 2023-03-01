package farjs.text.raw

import scommons.nodejs.Buffer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("iconv-lite", JSImport.Default)
object Iconv extends js.Object {

  def decode(buf: Buffer, encoding: String): String = js.native

  def encode(string: String, encoding: String): Buffer = js.native
}
