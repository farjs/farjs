package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("url", "URL")
class URL private[this](input: String, base: js.Any) extends js.Object {

  def this(input: String) = this(input, js.undefined)
  def this(input: String, base: String) = this(input, base: js.Any)
  def this(input: String, base: URL) = this(input, base: js.Any)
}
