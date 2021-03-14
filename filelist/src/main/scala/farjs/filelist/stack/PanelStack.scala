package farjs.filelist.stack

import farjs.filelist.stack.PanelStack.StackItem
import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.blessed.BlessedElement
import scommons.react.hooks._

import scala.scalajs.js

class PanelStack(val top: Option[StackItem],
                 updater: js.Function1[js.Function1[List[StackItem], List[StackItem]], Unit]) {

  def push(comp: ReactClass, params: js.Any): Unit = {
    updater { stack =>
      (comp, params) :: stack
    }
  }

  def update(params: js.Any): Unit = {
    updater { stack =>
      if (stack.nonEmpty) {
        val (comp, _) = stack.head
        (comp, params) :: stack.tail
      }
      else stack
    }
  }

  def pop(): Unit = {
    updater(_.tail)
  }

  def params[T]: T = top.map(_._2).orNull.asInstanceOf[T]
}

case class PanelStackProps(isRight: Boolean, panelInput: BlessedElement, width: Int = 0, height: Int = 0)

object PanelStack extends FunctionComponent[PanelStackProps] {
  
  type StackItem = (ReactClass, js.Any)

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
    val stacks = WithPanelStacks.usePanelStacks

    val maybeTop =
      if (props.isRight) stacks.rightStack.top
      else stacks.leftStack.top

    <(withSizeComp())(^.wrapped := WithSizeProps({ (width, height) =>
      <(PanelStack.Context.Provider)(^.contextValue := props.copy(width = width, height = height))(
        maybeTop match {
          case None => compProps.children
          case Some((comp, _)) => <(comp)()()
        }
      )
    }))()
  }
}
