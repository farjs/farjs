package farjs.filelist.stack

import scommons.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/stack/PanelStack.mjs", JSImport.Default)
class PanelStack(val isActive: Boolean,
                 data: js.Array[PanelStackItem[_]],
                 updater: js.Function1[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
                ) extends js.Object {

  def push[T](item: PanelStackItem[T]): Unit = js.native

  def update[T](f: js.Function1[PanelStackItem[T], PanelStackItem[T]]): Unit = js.native

  def updateFor[T](component: ReactClass, f: js.Function1[PanelStackItem[T], PanelStackItem[T]]): Unit = js.native

  def pop(): Unit = js.native

  def clear(): Unit = js.native

  def peek[T](): PanelStackItem[T] = js.native
  
  def peekLast[T](): PanelStackItem[T] = js.native
  
  def params[T](): T = js.native
}
