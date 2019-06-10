package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * https://nodejs.org/docs/latest-v9.x/api/path.html
  */
@js.native
@JSImport("path", JSImport.Default)
object Path extends Path

@js.native
trait Path extends js.Object {

  def isAbsolute(path: String): Boolean = js.native
  
  def join(paths: String*): String = js.native
  
  val win32: Path = js.native //when working with Windows file paths on any operating system
  val posix: Path = js.native //when working with POSIX file paths on any operating system
}
