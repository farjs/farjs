package farjs.ui

import farjs.ui.theme.DefaultTheme
import org.scalactic.source.Position
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class ListViewSpec extends TestSpec with TestRendererUtils {

  it should "call setViewport when props.height changes" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getListViewProps(index = 1, setViewport = setViewport)
    setViewport.expects(props.viewport)

    val renderer = createTestRenderer(<(ListView())(^.wrapped := props)())
    assertListView(renderer.root, props, List(
      "{bold}{white-fg}{cyan-bg}  item 1            {/}",
      "{bold}{white-fg}{black-bg}  item 2            {/}"
    ))
    val updatedProps = props.copy(height = 1)
    val expectedViewport = props.viewport.copy(offset = 1, focused = 0, viewLength = 1)

    //then
    setViewport.expects(expectedViewport)

    //when
    TestRenderer.act { () =>
      renderer.update(<(ListView())(^.wrapped := updatedProps)())
    }
  }

  it should "call onClick when onClick" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListViewProps(onClick = onAction)
    val mouseData = js.Dynamic.literal(y = 2)
    val textMock = js.Dynamic.literal(atop = 1)
    val renderer = createTestRenderer(<(ListView())(^.wrapped := props)(), { el =>
      if (el.`type` == <.text.name.asInstanceOf[js.Any]) textMock
      else null
    })
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }

    //then
    onAction.expects(1)
    
    //when
    text.props.onClick(mouseData)
  }

  it should "not call onClick if index >= length when onClick" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getListViewProps(onClick = onAction)
    val mouseData = js.Dynamic.literal(y = 3)
    val textMock = js.Dynamic.literal(atop = 1)
    val index = (mouseData.y - textMock.atop).asInstanceOf[Int]
    index should be >= props.items.length

    val renderer = createTestRenderer(<(ListView())(^.wrapped := props)(), { el =>
      if (el.`type` == <.text.name.asInstanceOf[js.Any]) textMock
      else null
    })
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }

    //then
    onAction.expects(*).never()
    
    //when
    text.props.onClick(mouseData)
  }

  it should "call setViewport when onWheelup" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getListViewProps(index = 1, setViewport = setViewport)
    setViewport.expects(props.viewport)

    val renderer = createTestRenderer(<(ListView())(^.wrapped := props)())
    assertListView(renderer.root, props, List(
      "{bold}{white-fg}{cyan-bg}  item 1            {/}",
      "{bold}{white-fg}{black-bg}  item 2            {/}"
    ))
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }
    val expectedViewport = props.viewport.copy(focused = 0)

    //then
    setViewport.expects(expectedViewport)

    //when
    text.props.onWheelup(null)
  }

  it should "call setViewport when onWheeldown" in {
    //given
    val setViewport = mockFunction[ListViewport, Unit]
    val props = getListViewProps(setViewport = setViewport)
    setViewport.expects(props.viewport)

    val renderer = createTestRenderer(<(ListView())(^.wrapped := props)())
    assertListView(renderer.root, props, List(
      "{bold}{white-fg}{black-bg}  item 1            {/}",
      "{bold}{white-fg}{cyan-bg}  item 2            {/}"
    ))
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }
    val expectedViewport = props.viewport.copy(focused = 1)

    //then
    setViewport.expects(expectedViewport)

    //when
    text.props.onWheeldown(null)
  }

  it should "render component" in {
    //given
    val props = getListViewProps(width = 10, items = List(
      "  dir\t1 {bold}",
      "  .dir 2 looooooong",
      "  .dir \r4",
      "  .file \n5",
      "  item"
    ))
    
    //when
    val result = createTestRenderer(<(ListView())(^.wrapped := props)()).root

    //then
    assertListView(result, props, List(
      "{bold}{white-fg}{black-bg}  dir 1 {open}b{/}",
      "{bold}{white-fg}{cyan-bg}  .dir 2 l{/}",
      "{bold}{white-fg}{cyan-bg}  .dir 4  {/}",
      "{bold}{white-fg}{cyan-bg}  .file 5 {/}",
      "{bold}{white-fg}{cyan-bg}  item    {/}"
    ))
  }

  private def getListViewProps(width: Int = 20,
                               height: Int = 30,
                               index: Int = 0,
                               items: List[String] = List(
                                 "  item 1",
                                 "  item 2"
                               ),
                               setViewport: js.Function1[ListViewport, Unit] = _ => (),
                               onClick: js.Function1[Int, Unit] = _ => ()): ListViewProps = {
    ListViewProps(
      left = 1,
      top = 1,
      width = width,
      height = height,
      items = js.Array(items: _*),
      viewport = ListViewport(index, items.size, height),
      setViewport = setViewport,
      style = DefaultTheme.popup.menu,
      onClick = onClick
    )
  }

  private def assertListView(result: TestInstance,
                             props: ListViewProps,
                             expectedContent: List[String])(implicit pos: Position): Unit = {

    assertComponents(result.children, List(
      <.text(
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbWidth := props.width,
        ^.rbHeight := props.height,
        ^.rbStyle := props.style,
        ^.rbTags := true,
        ^.content := expectedContent.mkString("\n")
      )()
    ))
  }
}
