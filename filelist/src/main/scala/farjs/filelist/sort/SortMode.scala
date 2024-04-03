package farjs.filelist.sort

import scala.scalajs.js

@js.native
sealed trait SortMode extends js.Object

object SortMode {
  
  val Name: SortMode = "Name".asInstanceOf[SortMode]
  val Extension: SortMode = "Extension".asInstanceOf[SortMode]
  val ModificationTime: SortMode = "ModificationTime".asInstanceOf[SortMode]
  val Size: SortMode = "Size".asInstanceOf[SortMode]
  val Unsorted: SortMode = "Unsorted".asInstanceOf[SortMode]
  val CreationTime: SortMode = "CreationTime".asInstanceOf[SortMode]
  val AccessTime: SortMode = "AccessTime".asInstanceOf[SortMode]
}
