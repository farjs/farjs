package farjs.filelist.stack

import scommons.react._

import scala.scalajs.js

class PanelStack(val isActive: Boolean,
                 data: js.Array[PanelStackItem[_]],
                 updater: js.Function1[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
                ) extends js.Object {

  def push[T](item: PanelStackItem[T]): Unit = updater(a => js.Array(item :: a.toList: _*))

  def update[T](f: js.Function1[PanelStackItem[T], PanelStackItem[T]]): Unit =
    updater { stack =>
      if (stack.isEmpty) stack
      else {
        js.Array(f(stack.head.asInstanceOf[PanelStackItem[T]]) :: stack.tail.toList: _*)
      }
    }

  def updateFor[T](component: ReactClass)(f: js.Function1[PanelStackItem[T], PanelStackItem[T]]): Unit =
    updater { stack =>
      stack.collect {
        case item if item.component == component =>
          f(item.asInstanceOf[PanelStackItem[T]])
        case item => item
      }
    }

  def pop(): Unit =
    updater {
      case a if a.length > 1 => js.Array(a.tail.toList: _*)
      case stack => stack
    }

  def clear(): Unit =
    updater {
      case a if a.length > 1 => js.Array(a.last)
      case stack => stack
    }

  def peek[T]: PanelStackItem[T] = data.head.asInstanceOf[PanelStackItem[T]]
  
  def peekLast[T]: PanelStackItem[T] = data.last.asInstanceOf[PanelStackItem[T]]
  
  def params[T]: T = peek[T].state.asInstanceOf[js.UndefOr[js.Any]].orNull.asInstanceOf[T]
}
