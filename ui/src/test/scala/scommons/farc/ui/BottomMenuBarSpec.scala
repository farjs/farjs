package scommons.farc.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

class BottomMenuBarSpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "call onClick callback when onClick" in {
    //given
    val onClick = mockFunction[String, Unit]
    val props = BottomMenuBarProps(onClick = onClick)
    val items = createTestRenderer(<(BottomMenuBar())(^.wrapped := props)()).root.children
    
    //then
    inSequence {
      onClick.expects("1")
      onClick.expects("2")
      onClick.expects("3")
      onClick.expects("4")
      onClick.expects("5")
      onClick.expects("6")
      onClick.expects("7")
      onClick.expects("8")
      onClick.expects("9")
      onClick.expects("10")
    }
    
    //when
    items(0).props.onClick(null)
    items(1).props.onClick(null)
    items(2).props.onClick(null)
    items(3).props.onClick(null)
    items(4).props.onClick(null)
    items(5).props.onClick(null)
    items(6).props.onClick(null)
    items(7).props.onClick(null)
    items(8).props.onClick(null)
    items(9).props.onClick(null)
  }
  
  it should "render component" in {
    //given
    val props = BottomMenuBarProps(onClick = _ => ())
    val comp = <(BottomMenuBar())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertNativeComponent(result,
      <.>()(
        items.map { case (id, k, name, pos) =>
          <.text(
            ^.key := id,
            ^.rbAutoFocus := false,
            ^.rbClickable := true,
            ^.rbTags := true,
            ^.rbMouse := true,
            ^.rbLeft := pos,
            ^.content := s"{white-fg}{black-bg}$k{/}{black-fg}{cyan-bg}$name{/}"
          )()
        }
      )
    )
  }

  private lazy val items: List[(String, String, String, Int)] = {
    List(
      (" 1", "       "),
      (" 2", "       "),
      (" 3", "       "),
      (" 4", "       "),
      (" 5", "       "),
      (" 6", "       "),
      (" 7", "       "),
      (" 8", "       "),
      (" 9", "       "),
      ("10", " Exit ")
    ).foldLeft(List.empty[(String, String, String, Int)]) { case (res, (k, name)) =>
      val pos =
        if (res.isEmpty) 0
        else {
          val (_, key, name, pos) = res.last
          pos + key.length + name.length
        }

      res :+ ((k.trim, k, name, pos))
    }
  }
}
