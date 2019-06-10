package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

/**
  * https://nodejs.org/docs/latest-v9.x/api/fs.html
  */
@js.native
@JSImport("fs", JSImport.Default)
object FS extends js.Object {

  def readdir(path: String | URL, callback: js.Function2[js.Error, js.Array[String], Unit]): Unit = js.native
  
  def lstatSync(path: String | URL): Stats = js.native
}
