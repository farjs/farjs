package farjs.viewer.quickview

import scala.scalajs.js

sealed trait QuickViewParams extends js.Object {
  val name: String
  val parent: String
  val folders: Double
  val files: Double
  val filesSize: Double
}

object QuickViewParams {

  def apply(name: String = "",
            parent: String = "",
            folders: Double = 0.0,
            files: Double = 0.0,
            filesSize: Double = 0.0): QuickViewParams = {

    js.Dynamic.literal(
      name = name,
      parent = parent,
      folders = folders,
      files = files,
      filesSize = filesSize
    ).asInstanceOf[QuickViewParams]
  }

  def unapply(arg: QuickViewParams): Option[(String, String, Double, Double, Double)] = {
    Some((
      arg.name,
      arg.parent,
      arg.folders,
      arg.files,
      arg.filesSize
    ))
  }

  def copy(p: QuickViewParams)(name: String = p.name,
                               parent: String = p.parent,
                               folders: Double = p.folders,
                               files: Double = p.files,
                               filesSize: Double = p.filesSize): QuickViewParams = {
    QuickViewParams(
      name = name,
      parent = parent,
      folders = folders,
      files = files,
      filesSize = filesSize
    )
  }
}
