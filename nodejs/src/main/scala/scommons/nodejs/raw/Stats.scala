package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("fs", "Stats")
abstract class Stats extends js.Object {

  def isDirectory(): Boolean = js.native
  def isFile(): Boolean = js.native
  def isSymbolicLink(): Boolean = js.native

  val size: Double = js.native //The size of the file in bytes.

  val atimeMs: Double = js.native //the last time file data was accessed, in milliseconds
  val mtimeMs: Double = js.native //the last time file data was modified, in milliseconds
  val ctimeMs: Double = js.native //the last time file status was changed, in milliseconds
  val birthtimeMs: Double = js.native //the creation time of file, in milliseconds
}
