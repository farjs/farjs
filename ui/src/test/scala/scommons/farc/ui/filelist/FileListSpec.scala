package scommons.farc.ui.filelist

import org.scalactic.source.Position
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class FileListSpec extends TestSpec with ShallowRendererUtils {

  it should "focus item when onWheelup/onWheeldown" in {
    //given
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5"
    ), FileListState(), onStateChanged = onStateChanged)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(up: Boolean, offset: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Unit = {
      val state = FileListState(offset, index)
      if (changed) {
        //then
        onStateChanged.expects(state)
      }
      
      //when
      if (up) findComponentProps(renderer.getRenderOutput(), FileListView).onWheelUp()
      else findComponentProps(renderer.getRenderOutput(), FileListView).onWheelDown()

      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val viewProps = findComponentProps(renderer.getRenderOutput(), FileListView)
      viewProps.focusedIndex shouldBe index
    }
    
    //when & then
    check(up = false, offset = 1, index = 0)
    check(up = false, offset = 1, index = 1)
    check(up = false, offset = 1, index = 2)
    check(up = false, offset = 1, index = 3)
    check(up = false, offset = 1, index = 3, changed = false)

    //when & then
    check(up = true, offset = 0, index = 3)
    check(up = true, offset = 0, index = 2)
    check(up = true, offset = 0, index = 1)
    check(up = true, offset = 0, index = 0)
    check(up = true, offset = 0, index = 0, changed = false)
  }

  it should "focus item when onClick" in {
    //given
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ), FileListState(), onStateChanged = onStateChanged)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(clickIndex: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Unit = {
      val state = FileListState(0, index)
      if (changed) {
        //then
        onStateChanged.expects(state)
      }

      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onClick(clickIndex)
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val viewProps = findComponentProps(renderer.getRenderOutput(), FileListView)
      viewProps.focusedIndex shouldBe index
    }
    
    //when & then
    check(clickIndex = 0, index = 0, changed = false) // first item in col 1
    check(clickIndex = 1, index = 1) // second item in col 1
    check(clickIndex = 2, index = 2) // first item in col 2
    check(clickIndex = 3, index = 2, changed = false) // last item in col 2
  }

  it should "focus and select item when onKeypress" in {
    //given
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps((7, 3), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3",
      4 -> "item 4",
      5 -> "item 5",
      6 -> "item 6",
      7 -> "item 7"
    ), FileListState(), onStateChanged = onStateChanged)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0
    
    def check(keyFull: String,
              items: List[(Int, String)],
              offset: Int,
              index: Int,
              selectedIds: Set[Int],
              changed: Boolean = true
             )(implicit pos: Position): Unit = {

      val state = FileListState(offset, index, selectedIds)
      if (changed) {
        //then
        onStateChanged.expects(state)
      }
      
      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onKeypress(keyFull)
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val viewProps = findComponentProps(renderer.getRenderOutput(), FileListView)
      (viewProps.items, viewProps.focusedIndex, viewProps.selectedIds) shouldBe ((items, index, selectedIds))
    }
    
    //when & then
    check("unknown", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty, changed = false)
    
    //when & then
    check("S-down",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 1, Set(1))
    check("S-down",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 2, Set(1, 2))
    check("down",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 3, Set(1, 2))
    check("down",    List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 1, 3, Set(1, 2))
    check("S-down",  List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 2, 3, Set(1, 2, 5))
    check("S-down",  List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 5, 6, 7))
    check("down",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 5, 6, 7), changed = false)

    //when & then
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 2, Set(1, 2, 5, 6))
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 1, Set(1, 2, 5))
    check("S-up",    List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 0, Set(1, 2))
    check("up",      List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 2, 0, Set(1, 2))
    check("up",      List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 1, 0, Set(1, 2))
    check("S-up",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty)
    check("up",      List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty, changed = false)

    //when & then
    check("S-right", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 2, Set(1, 2))
    check("right",   List(3 -> "item 3", 4 -> "item 4", 5 -> "item 5", 6 -> "item 6"), 2, 2, Set(1, 2))
    check("S-right", List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 5, 6, 7))
    check("right",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 5, 6, 7), changed = false)

    //when & then
    check("S-left",  List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 1, Set(1, 2, 5))
    check("left",    List(2 -> "item 2", 3 -> "item 3", 4 -> "item 4", 5 -> "item 5"), 1, 1, Set(1, 2, 5))
    check("S-left",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set(1, 2, 3, 5))
    check("left",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set(1, 2, 3, 5), changed = false)

    //when & then
    check("S-pagedown", List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 3, Set(5))
    check("S-pagedown", List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(4, 5, 6, 7))
    check("pagedown",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(4, 5, 6, 7), changed = false)

    //when & then
    check("S-pageup",List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 0, Set(4))
    check("S-pageup",List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty)
    check("pageup",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty, changed = false)

    //when & then
    check("end",     List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set.empty)
    check("end",     List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set.empty, changed = false)

    //when & then
    check("home",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty)
    check("home",    List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty, changed = false)
    
    //when & then
    check("S-end",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 3, 4, 5, 6, 7))
    check("S-end",   List(4 -> "item 4", 5 -> "item 5", 6 -> "item 6", 7 -> "item 7"), 3, 3, Set(1, 2, 3, 4, 5, 6, 7), changed = false)

    //when & then
    check("S-home",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty)
    check("S-home",  List(1 -> "item 1", 2 -> "item 2", 3 -> "item 3", 4 -> "item 4"), 0, 0, Set.empty, changed = false)
  }

  it should "render empty component" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = Nil, FileListState(), _ => ())
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = Nil,
      focusedIndex = 0,
      selectedIds = Set.empty
    )
  }
  
  it should "render non-empty component" in {
    //given
    val props = FileListProps((7, 2), columns = 2, items = List(
      1 -> "item 1",
      2 -> "item 2",
      3 -> "item 3"
    ), FileListState(), _ => ())
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = List(1 -> "item 1", 2 -> "item 2"),
      focusedIndex = 0,
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
