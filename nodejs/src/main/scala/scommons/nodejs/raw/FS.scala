package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("fs", JSImport.Default)
object FS extends js.Object {

  def readdir(path: URL, callback: js.Function2[js.Error, js.Array[String], Unit]): Unit = js.native
  
  def lstatSync(path: URL): Stats = js.native
}
