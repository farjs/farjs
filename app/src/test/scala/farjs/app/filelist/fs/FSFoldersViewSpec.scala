package farjs.app.filelist.fs

import farjs.ui.theme.DefaultTheme
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js.Dynamic.literal

class FSFoldersViewSpec extends TestSpec with TestRendererUtils {

  it should "update viewport state when onKeypress" in {
    //given
    val props = getFSFoldersViewProps()
    val renderer = createTestRenderer(<(FSFoldersView())(^.wrapped := props)())
    assertFSFoldersView(renderer.root, props,
      """{bold}{white-fg}{black-bg}  item 1            {/}
        |{bold}{white-fg}{cyan-bg}  item 2            {/}""".stripMargin
    )
    val button = inside(findComponents(renderer.root, <.button.name)) {
      case List(button) => button
    }
    
    //when
    button.props.onKeypress(null, literal(full = "down"))
    
    //then
    assertFSFoldersView(renderer.root, props,
      """{bold}{white-fg}{cyan-bg}  item 1            {/}
        |{bold}{white-fg}{black-bg}  item 2            {/}""".stripMargin
    )
  }

  it should "render component" in {
    //given
    val props = getFSFoldersViewProps(width = 10, items = List(
      "dir\t1 {bold}",
      ".dir 2 looooooong",
      ".dir \r4",
      ".file \n5",
      "item"
    ))
    
    //when
    val result = createTestRenderer(<(FSFoldersView())(^.wrapped := props)()).root

    //then
    assertFSFoldersView(result, props,
      """{bold}{white-fg}{black-bg}  dir 1 {open}b{/}
        |{bold}{white-fg}{cyan-bg}  .dir 2 l{/}
        |{bold}{white-fg}{cyan-bg}  .dir 4  {/}
        |{bold}{white-fg}{cyan-bg}  .file 5 {/}
        |{bold}{white-fg}{cyan-bg}  item    {/}""".stripMargin
    )
  }

  private def getFSFoldersViewProps(width: Int = 20,
                                    height: Int = 30,
                                    selected: Int = 0,
                                    items: List[String] = List(
                                      "item 1",
                                      "item 2"
                                    ),
                                    onAction: String => Unit = _ => ()): FSFoldersViewProps = {
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
                                  expectedContent: String): Unit = {
    assertComponents(result.children, List(
      <.button(
        ^.rbMouse := true,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbWidth := props.width,
        ^.rbHeight := props.height
      )(
        <.text(
          ^.rbWidth := props.width,
          ^.rbHeight := props.height,
          ^.rbStyle := props.style,
          ^.rbTags := true,
          ^.content := expectedContent
        )()
      )
    ))
  }
}
