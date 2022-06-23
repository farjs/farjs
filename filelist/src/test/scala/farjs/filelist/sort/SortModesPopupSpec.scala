package farjs.filelist.sort

import farjs.filelist.sort.SortModesPopup._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import scommons.react.blessed.BlessedElement
import scommons.react.test._

import scala.scalajs.js

class SortModesPopupSpec extends TestSpec with TestRendererUtils {

  SortModesPopup.menuPopup = mockUiComponent("MenuPopup")
  
  it should "emit keypress event and call onClose when onSelect" in {
    //given
    val stack = new PanelStack(isActive = true, Nil, null)
    val onClose = mockFunction[Unit]
    val emitMock = mockFunction[String, js.Any, js.Dynamic, Boolean]
    val inputMock = js.Dynamic.literal("emit" -> emitMock).asInstanceOf[BlessedElement]
    val props = SortModesPopupProps(SortMode.Name, ascending = true, onClose)
    val comp = testRender(withContext(
      <(SortModesPopup())(^.wrapped := props)(), stack = stack, width = 40, panelInput = inputMock
    ))
    val menuProps = findComponentProps(comp, menuPopup)

    //then
    onClose.expects()
    emitMock.expects("keypress", *, *).onCall { (_, _, key) =>
      key.name shouldBe "f3+ctrl"
      key.full shouldBe "C-f3"
      key.ctrl shouldBe true
      false
    }

    //when
    menuProps.onSelect(0)
  }
  
  it should "render popup on left panel" in {
    //given
    val isRight = false
    val stack = new PanelStack(isActive = true, Nil, null)
    val width = 40
    val props = SortModesPopupProps(SortMode.Extension, ascending = false, () => ())

    //when
    val result = testRender(withContext(
      <(SortModesPopup())(^.wrapped := props)(), stack = stack, isRight = isRight, width = width
    ))

    //then
    assertSortModesPopup(result, props, isRight, width)
  }
  
  it should "render popup on right panel" in {
    //given
    val isRight = true
    val stack = new PanelStack(isActive = false, Nil, null)
    val width = 40
    val props = SortModesPopupProps(SortMode.Extension, ascending = false, () => ())

    //when
    val result = testRender(withContext(
      <(SortModesPopup())(^.wrapped := props)(), stack = stack, isRight = isRight, width = width
    ))

    //then
    assertSortModesPopup(result, props, isRight, width)
  }
  
  private def assertSortModesPopup(result: TestInstance,
                                   props: SortModesPopupProps,
                                   isRight: Boolean,
                                   stackWidth: Int): Unit = {

    assertTestComponent(result, menuPopup) {
      case MenuPopupProps(title, items, getLeft, _, onClose) =>
        title shouldBe "Sort by"
        items shouldBe List(
          "  Name                 Ctrl-F3  ",
          "- Extension            Ctrl-F4  ",
          "  Modification time    Ctrl-F5  ",
          "  Size                 Ctrl-F6  ",
          "  Unsorted             Ctrl-F7  ",
          "  Creation time        Ctrl-F8  ",
          "  Access time          Ctrl-F9  "
        )
        getLeft(50) shouldBe MenuPopup.getLeftPos(stackWidth, !isRight, 50)
        onClose should be theSameInstanceAs props.onClose
    }
  }
}
