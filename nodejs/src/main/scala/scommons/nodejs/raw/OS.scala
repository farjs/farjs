package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("os", JSImport.Default)
object OS extends OS

@js.native
trait OS extends js.Object {

  def homedir(): String = js.native
}
