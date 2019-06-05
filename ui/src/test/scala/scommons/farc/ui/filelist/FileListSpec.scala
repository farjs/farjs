package scommons.farc.ui.filelist

import org.scalactic.source.Position
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class FileListSpec extends TestSpec with ShallowRendererUtils {

  it should "focus item when onWheelup/onWheeldown" in {
    //given
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5"
    ))
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe -1

    def check(up: Boolean, focusedIndex: Int)(implicit pos: Position): Unit = {
      if (up) findComponentProps(renderer.getRenderOutput(), FileListView).onWheelUp()
      else findComponentProps(renderer.getRenderOutput(), FileListView).onWheelDown()

      val props = findComponentProps(renderer.getRenderOutput(), FileListView)
      props.focusedIndex shouldBe focusedIndex
    }
    
    //when & then
    check(up = false, focusedIndex = 0)
    check(up = false, focusedIndex = 1)
    check(up = false, focusedIndex = 2)
    check(up = false, focusedIndex = 3)
    check(up = false, focusedIndex = 3)
    check(up = false, focusedIndex = 3) //noop

    //when & then
    check(up = true, focusedIndex = 3)
    check(up = true, focusedIndex = 2)
    check(up = true, focusedIndex = 1)
    check(up = true, focusedIndex = 0)
    check(up = true, focusedIndex = 0) //noop
  }

  it should "focus item when onClick" in {
    //given
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe -1

    def check(clickIndex: Int, focusedIndex: Int)(implicit pos: Position): Unit = {
      findComponentProps(renderer.getRenderOutput(), FileListView).onClick(clickIndex)

      val props = findComponentProps(renderer.getRenderOutput(), FileListView)
      props.focusedIndex shouldBe focusedIndex
    }
    
    //when & then
    check(clickIndex = 0, focusedIndex = 0) // first item in col 1
    check(clickIndex = 1, focusedIndex = 1) // second item in col 1
    check(clickIndex = 2, focusedIndex = 2) // first item in col 2
    check(clickIndex = 3, focusedIndex = 2) // last item in col 2 (noop)
  }

  it should "focus and select item when onKeypress" in {
    //given
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5",
      6 -> "item 6",
      7 -> "item 7"
    ))
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe -1
    
    def check(keyFull: String,
              items: List[(Int, String)],
              focusedIndex: Int,
              selectedIds: Set[Int]
             )(implicit pos: Position): Unit = {

      findComponentProps(renderer.getRenderOutput(), FileListView).onKeypress(keyFull)

      val props = findComponentProps(renderer.getRenderOutput(), FileListView)
      (props.items, props.focusedIndex, props.selectedIds) shouldBe ((items, focusedIndex, selectedIds))
    }
    
    //when & then
    check("unknown", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), -1, Set.empty) //noop
    
    //when & then
    check("down",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    check("S-down",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 1, Set(1))
    check("S-down",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 2, Set(1, 2))
    check("down",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 3, Set(1, 2))
    check("down",    List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 3, Set(1, 2))
    check("S-down",  List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 3, Set(1, 2, 5))
    check("S-down",  List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 5, 6, 7))
    check("down",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 5, 6, 7)) //noop

    //when & then
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 2, Set(1, 2, 5, 6))
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 1, Set(1, 2, 5))
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 0, Set(1, 2))
    check("up",      List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 0, Set(1, 2))
    check("up",      List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 0, Set(1, 2))
    check("S-up",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    check("up",      List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty) //noop

    //when & then
    check("S-right", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 2, Set(1, 2))
    check("right",   List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 2, Set(1, 2))
    check("S-right", List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 5, 6, 7))
    check("right",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 5, 6, 7)) //noop

    //when & then
    check("S-left",  List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 1, Set(1, 2, 5))
    check("left",    List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 1, Set(1, 2, 5))
    check("S-left",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set(1, 2, 3, 5))
    check("left",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set(1, 2, 3, 5)) //noop

    //when & then
    check("S-pagedown", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 3, Set(5))
    check("S-pagedown", List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(4, 5, 6, 7))
    check("pagedown",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(4, 5, 6, 7)) //noop

    //when & then
    check("S-pageup",List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 0, Set(4))
    check("S-pageup",List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    check("pageup",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty) //noop

    //when & then
    check("S-end",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 3, 4, 5, 6, 7))
    check("end",     List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, Set(1, 2, 3, 4, 5, 6, 7)) //noop

    //when & then
    check("S-home",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty)
    check("home",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, Set.empty) //noop
  }

  it should "render empty component" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = Nil)
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = Nil,
      focusedIndex = -1,
      selectedIds = Set.empty
    )
  }
  
  it should "render non-empty component" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ))
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = List(1 -> "item 1", 2 -> "item 2"),
      focusedIndex = -1,
      selectedIds = Set.empty
    )
  }
  
  private def assertFileList(result: ShallowInstance,
                             props: FileListProps,
                             viewItems: List[(Int, String)],
                             focusedIndex: Int,
                             selectedIds: Set[Int]): Unit = {
    
    assertComponent(result, FileListView) {
      case FileListViewProps(resSize, columns, items, resFocusedIndex, resSelectedIds, _, _, _, _) =>
        resSize shouldBe props.size
        columns shouldBe props.columns
        items shouldBe viewItems
        resFocusedIndex shouldBe focusedIndex
        resSelectedIds shouldBe selectedIds
    }
  }
}
