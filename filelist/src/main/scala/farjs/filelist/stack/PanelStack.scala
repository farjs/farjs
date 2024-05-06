package farjs.filelist.stack

import scommons.react._

import scala.scalajs.js

class PanelStack(val isActive: Boolean,
                 data: List[PanelStackItem[_]],
                 updater: js.Function1[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]) {

  def push[T](item: PanelStackItem[T]): Unit = updater(item :: _)

  def update[T](f: PanelStackItem[T] => PanelStackItem[T]): Unit =
    updater { stack =>
      if (stack.isEmpty) stack
      else f(stack.head.asInstanceOf[PanelStackItem[T]]) :: stack.tail
    }

  def updateFor[T](component: ReactClass)(f: PanelStackItem[T] => PanelStackItem[T]): Unit =
    updater { stack =>
      stack.collect {
        case item if item.component == component =>
          f(item.asInstanceOf[PanelStackItem[T]])
        case item => item
      }
    }

  def pop(): Unit =
    updater {
      case _ :: tail if tail.nonEmpty => tail
      case stack => stack
    }

  def clear(): Unit =
    updater {
      case _ :: tail if tail.nonEmpty => tail.last :: Nil
      case stack => stack
    }

  def peek[T]: PanelStackItem[T] = data.head.asInstanceOf[PanelStackItem[T]]
  
  def peekLast[T]: PanelStackItem[T] = data.last.asInstanceOf[PanelStackItem[T]]
  
  def params[T]: T = peek[T].state.asInstanceOf[Option[js.Any]].orNull.asInstanceOf[T]
}
