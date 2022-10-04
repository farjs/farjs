package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersView._
import farjs.ui.{ScrollBarProps, UI}
import farjs.ui.theme.DefaultTheme
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class FSFoldersViewSpec extends TestSpec with TestRendererUtils {

  FSFoldersView.scrollBarComp = mockUiComponent("ScrollBar")

  it should "scroll when onChange in ScrollBar" in {
    //given
    val props = getFSFoldersViewProps(height = 1)
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props, showScrollBar = true,
      """{bold}{white-fg}{black-bg}  item 1            {/}"""
    )
    val scrollBarProps = findComponentProps(renderer.root, scrollBarComp, plain = true)
    
    //when
    scrollBarProps.onChange(1)
    
    //then
    assertFSFoldersView(renderer.root, props.copy(selected = 1), showScrollBar = true,
      """{bold}{white-fg}{black-bg}  item 2            {/}"""
    )
  }

  it should "update viewport state when props.height changes" in {
    //given
    val props = getFSFoldersViewProps(selected = 1)
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{cyan-bg}  item 1            {/}
        |{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
    val updatedProps = props.copy(height = 1)
    
    //when
    TestRenderer.act { () =>
      renderer.update(<(FSFoldersView())(^.wrapped := updatedProps)())
    }
    
    //then
    assertFSFoldersView(renderer.root, updatedProps, showScrollBar = true,
      """{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
  }

  it should "update viewport state when onKeypress(down)" in {
    //given
    val props = getFSFoldersViewProps()
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{black-bg}  item 1            {/}
        |{bold}{white-fg}{cyan-bg}  item 2            {/}""".stripMargin
    )
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button) => button
    }
    
    //when
    button.props.onKeypress(null, literal(full = "down"))
    
    //then
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{cyan-bg}  item 1            {/}
        |{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
  }

  it should "call onAction when onKeypress(return)" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getFSFoldersViewProps(selected = 1, onAction = onAction)
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button) => button
    }

    //then
    onAction.expects(1)

    //when
    button.props.onKeypress(null, literal(full = "return"))
  }

  it should "call onAction when onClick" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getFSFoldersViewProps(onAction = onAction)
    val mouseData = js.Dynamic.literal(y = 2)
    val textMock = js.Dynamic.literal(atop = 1)
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)(), { el =>
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

  it should "not call onAction if index >= length when onClick" in {
    //given
    val onAction = mockFunction[Int, Unit]
    val props = getFSFoldersViewProps(onAction = onAction)
    val mouseData = js.Dynamic.literal(y = 3)
    val textMock = js.Dynamic.literal(atop = 1)
    val index = (mouseData.y - textMock.atop).asInstanceOf[Int]
    index should be >= props.items.length

    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)(), { el =>
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

  it should "update viewport state when onWheelup" in {
    //given
    val props = getFSFoldersViewProps(selected = 1)
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{cyan-bg}  item 1            {/}
        |{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }
    
    //when
    text.props.onWheelup(null)
    
    //then
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{black-bg}  item 1            {/}
        |{bold}{white-fg}{cyan-bg}  item 2            {/}""".stripMargin
    )
  }

  it should "update viewport state when onWheeldown" in {
    //given
    val props = getFSFoldersViewProps()
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{black-bg}  item 1            {/}
        |{bold}{white-fg}{cyan-bg}  item 2            {/}""".stripMargin
    )
    val text = inside(findComponents(renderer.root, <.text.name)) {
      case List(text) => text
    }
    
    //when
    text.props.onWheeldown(null)
    
    //then
    assertFSFoldersView(renderer.root, props, showScrollBar = false,
      """{bold}{white-fg}{cyan-bg}  item 1            {/}
        |{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
  }

  it should "render without ScrollBar" in {
    //given
    val props = getFSFoldersViewProps(width = 10, items = List(
      "  dir\t1 {bold}",
      "  .dir 2 looooooong",
      "  .dir \r4",
      "  .file \n5",
      "  item"
    ))
    
    //when
    val result = createTestRenderer(<(FSFoldersView())(^.wrapped := props)()).root

    //then
    assertFSFoldersView(result, props, showScrollBar = false,
      """{bold}{white-fg}{black-bg}  dir 1 {open}b{/}
        |{bold}{white-fg}{cyan-bg}  .dir 2 l{/}
        |{bold}{white-fg}{cyan-bg}  .dir 4  {/}
        |{bold}{white-fg}{cyan-bg}  .file 5 {/}
        |{bold}{white-fg}{cyan-bg}  item    {/}""".stripMargin
    )
  }

  it should "render with ScrollBar" in {
    //given
    val props = getFSFoldersViewProps(width = 5, height = 20, items = List.fill(25)("item"))
    
    //when
    val result = createTestRenderer(<(FSFoldersView())(^.wrapped := props)()).root

    //then
    assertFSFoldersView(result, props, showScrollBar = true,
      ("{bold}{white-fg}{black-bg}item {/}" :: List.fill(19)(
        "{bold}{white-fg}{cyan-bg}item {/}"
      )).mkString(UI.newLine)
    )
  }

  private def getFSFoldersViewProps(width: Int = 20,
                                    height: Int = 30,
                                    selected: Int = 0,
                                    items: List[String] = List(
                                      "  item 1",
                                      "  item 2"
                                    ),
                                    onAction: Int => Unit = _ => ()): FSFoldersViewProps = {
    FSFoldersViewProps(
      left = 1,
      top = 1,
      width = width,
      height = height,
      selected = selected,
      items = items,
      style = DefaultTheme.popup.menu,
      onAction = onAction
    )
  }
  
  private def assertFSFoldersView(result: TestInstance,
                                  props: FSFoldersViewProps,
                                  showScrollBar: Boolean,
                                  expectedContent: String): Unit = {
    assertComponents(result.children, List(
      <.button(
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbWidth := props.width,
        ^.rbHeight := props.height
      )(
        <.text(
          ^.rbClickable := true,
          ^.rbMouse := true,
          ^.rbAutoFocus := false,
          ^.rbWidth := props.width,
          ^.rbHeight := props.height,
          ^.rbStyle := props.style,
          ^.rbTags := true,
          ^.content := expectedContent
        )(),

        if (showScrollBar) Some {
          <(scrollBarComp())(^.assertPlain[ScrollBarProps](inside(_) {
            case ScrollBarProps(left, top, length, style, value, extent, min, max, _) =>
              left shouldBe (props.left + props.width - 1)
              top shouldBe (props.top - 1)
              length shouldBe props.height
              style shouldBe props.style
              value shouldBe props.selected
              extent shouldBe props.height
              min shouldBe 0
              max shouldBe (props.items.size - props.height)
          }))()
        }
        else None
      )
    ))
  }
}
