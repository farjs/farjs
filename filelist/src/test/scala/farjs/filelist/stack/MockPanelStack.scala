package farjs.filelist.stack

import farjs.filelist.stack.PanelStack.StackItem
import scommons.react.ReactClass

//noinspection NotImplementedCode
class MockPanelStack[P](
  pushMock: (ReactClass, P) => Unit = (_: ReactClass, _: P) => ???,
  updateMock: P => Unit = (_: P) => ???,
  popMock: () => Unit = () => ???,
  peekMock: () => Option[StackItem] = () => ???,
  paramsMock: () => P = () => ???
) extends PanelStack(None, _ => ???) {

  override def push[T](comp: ReactClass, params: T): Unit =
    pushMock(comp, params.asInstanceOf[P])
  
  override def update[T](params: T): Unit =
    updateMock(params.asInstanceOf[P])

  override def pop(): Unit = popMock()

  override def peek: Option[StackItem] = peekMock()

  override def params[T]: T = paramsMock().asInstanceOf[T]
}
