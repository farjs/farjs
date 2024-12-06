package farjs.filelist.stack

import scala.scalajs.js

//noinspection NotImplementedCode
class MockPanelStack[P](
  isActive: Boolean = false,
  pushMock: PanelStackItem[P] => Unit = (_: PanelStackItem[P]) => ???,
  updateMock: js.Function1[PanelStackItem[P], PanelStackItem[P]] => Unit = (_: js.Function1[PanelStackItem[P], PanelStackItem[P]]) => ???,
  popMock: () => Unit = () => ???,
  clearMock: () => Unit = () => ???,
  peekMock: () => PanelStackItem[P] = () => ???,
  peekLastMock: () => PanelStackItem[P] = () => ???,
  paramsMock: () => P = () => ???
) extends PanelStack(isActive, js.Array(), _ => ???) {

  override def push[T](item: PanelStackItem[T]): Unit =
    pushMock(item.asInstanceOf[PanelStackItem[P]])
  
  override def update[T](f: js.Function1[PanelStackItem[T], PanelStackItem[T]]): Unit =
    updateMock(f.asInstanceOf[js.Function1[PanelStackItem[P], PanelStackItem[P]]])

  override def pop(): Unit = popMock()

  override def clear(): Unit = clearMock()

  override def peek[T](): PanelStackItem[T] =
    peekMock().asInstanceOf[PanelStackItem[T]]
  
  override def peekLast[T](): PanelStackItem[T] =
    peekLastMock().asInstanceOf[PanelStackItem[T]]

  override def params[T](): T = paramsMock().asInstanceOf[T]
}
