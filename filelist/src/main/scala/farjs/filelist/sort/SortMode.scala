package farjs.filelist.sort

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait SortMode extends js.Object

@js.native
@JSImport("@farjs/filelist/sort/SortMode.mjs", JSImport.Default)
object SortMode extends js.Object {
  
  val Name: SortMode = js.native
  val Extension: SortMode = js.native
  val ModificationTime: SortMode = js.native
  val Size: SortMode = js.native
  val Unsorted: SortMode = js.native
  val CreationTime: SortMode = js.native
  val AccessTime: SortMode = js.native
}
