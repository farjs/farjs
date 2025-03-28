package farjs.copymove

import scala.scalajs.js

sealed trait FileExistsAction extends js.Object

object FileExistsAction {

  val Overwrite: FileExistsAction = "Overwrite".asInstanceOf[FileExistsAction]
  val All: FileExistsAction = "All".asInstanceOf[FileExistsAction]
  val Skip: FileExistsAction = "Skip".asInstanceOf[FileExistsAction]
  val SkipAll: FileExistsAction = "SkipAll".asInstanceOf[FileExistsAction]
  val Append: FileExistsAction = "Append".asInstanceOf[FileExistsAction]
}
