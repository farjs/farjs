package scommons.farc.ui.filelist

import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class FileListItemSpec extends TestSpec with ShallowRendererUtils {

  it should "render not focused short item" in {
    //given
    val props = getFileListItemProps.copy(
      width = 10,
      text = "short item",
      focused = false
    )
    val comp = <(FileListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListItem(result, props, longItem = false)
  }
  
  it should "render not focused too long item" in {
    //given
    val props = getFileListItemProps.copy(
      width = 3,
      text = "too long item",
      focused = false
    )
    val comp = <(FileListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListItem(result, props, longItem = true)
  }
  
  it should "render focused short item" in {
    //given
    val props = getFileListItemProps.copy(
      width = 10,
      text = "short item",
      focused = true
    )
    val comp = <(FileListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListItem(result, props, longItem = false)
  }
  
  it should "render focused long item" in {
    //given
    val props = getFileListItemProps.copy(
      width = 3,
      text = "too long item",
      focused = true
    )
    val comp = <(FileListItem())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileListItem(result, props, longItem = true)
  }
  
  private def getFileListItemProps: FileListItemProps = FileListItemProps(
    width = 5,
    top = 2,
    style = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {}
    },
    text = "test item",
    focused = true
  )

  private def assertFileListItem(result: ShallowInstance,
                                 props: FileListItemProps,
                                 longItem: Boolean): Unit = {
    assertNativeComponent(result,
      <.>()(
        <.text(
          ^.key := "text",
          ^.rbWidth := props.width,
          ^.rbHeight := 1,
          ^.rbTop := props.top,
          ^.rbStyle := {
            if (props.focused) props.style.focus.asInstanceOf[BlessedStyle]
            else props.style
          },
          ^.content := props.text.take(props.width)
        )(),
        
        if (longItem) Some(
          <.text(
            ^.key := "longMark",
            ^.rbHeight := 1,
            ^.rbLeft := props.width,
            ^.rbTop := props.top,
            ^.rbStyle := new BlessedStyle {
              override val fg = "red"
              override val bg = "blue"
            },
            ^.content := "}"
          )()
        )
        else None
      )
    )
  }
}
