package farjs.filelist.stack

import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.blessed.BlessedElement
import scommons.react.hooks._

import scala.scalajs.js

class PanelStack(val isActive: Boolean,
                 data: List[PanelStackItem[_]],
                 updater: js.Function1[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]) {

  def push[T](item: PanelStackItem[T]): Unit = updater(item :: _)

  def update[T](f: PanelStackItem[T] => PanelStackItem[T]): Unit = {
    updater { stack =>
      if (stack.isEmpty) stack
      else f(stack.head.asInstanceOf[PanelStackItem[T]]) :: stack.tail
    }
  }

  def pop(): Unit = {
    updater {
      case _ :: tail if tail.nonEmpty => tail
      case stack => stack
    }
  }

  def peek[T]: PanelStackItem[T] = data.head.asInstanceOf[PanelStackItem[T]]
  
  def peekLast[T]: PanelStackItem[T] = data.last.asInstanceOf[PanelStackItem[T]]
  
  def params[T]: T = peek[T].state.asInstanceOf[Option[js.Any]].orNull.asInstanceOf[T]
}

case class PanelStackProps(isRight: Boolean,
                           panelInput: BlessedElement,
                           stack: PanelStack,
                           width: Int = 0,
                           height: Int = 0)

object PanelStack extends FunctionComponent[PanelStackProps] {
  
  val Context: ReactContext[PanelStackProps] = ReactContext[PanelStackProps](defaultValue = null)

  private[stack] var withSizeComp: UiComponent[WithSizeProps] = WithSize

  def usePanelStack: PanelStackProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "PanelStack.Context is not found." +
          "\nPlease, make sure you use PanelStack and not creating nested stacks."
      ))
    }
    ctx
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val topComp = props.stack.peek.component

    <(withSizeComp())(^.wrapped := WithSizeProps({ (width, height) =>
      <(PanelStack.Context.Provider)(^.contextValue := props.copy(width = width, height = height))(
        <(topComp)()(),
        compProps.children
      )
    }))()
  }
}
