package farjs.fs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait FSDisk extends js.Object {
  val root: String
  val size: Double
  val free: Double
  val name: String
}

@js.native
@JSImport("../fs/FSDisk.mjs", JSImport.Default)
object FSDiskNative extends js.Object {

  def fromDfCommand(output: String): js.Array[FSDisk] = js.native

  def fromWmicLogicalDisk(output: String): js.Array[FSDisk] = js.native
}


object FSDisk {

  def apply(root: String, size: Double, free: Double, name: String): FSDisk = {
    js.Dynamic.literal(
      root = root,
      size = size,
      free = free,
      name = name
    ).asInstanceOf[FSDisk]
  }

  def unapply(arg: FSDisk): Option[(String, Double, Double, String)] = {
    Some((
      arg.root,
      arg.size,
      arg.free,
      arg.name
    ))
  }

  def copy(p: FSDisk)(root: String = p.root,
                      size: Double = p.size,
                      free: Double = p.free,
                      name: String = p.name): FSDisk = {
    FSDisk(
      root = root,
      size = size,
      free = free,
      name = name
    )
  }

  def fromDfCommand(output: String): js.Array[FSDisk] =
    FSDiskNative.fromDfCommand(output)
    
  def fromWmicLogicalDisk(output: String): js.Array[FSDisk] =
    FSDiskNative.fromWmicLogicalDisk(output)
}
