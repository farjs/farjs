package farjs.filelist

import farjs.filelist.FileList._
import farjs.filelist.FileListActions._
import farjs.filelist.api.FileListItemSpec.assertFileListItems
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.Dispatch
import farjs.ui.task.Task
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed.BlessedScreen
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FileListSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FileList.fileListViewComp = mockUiComponent("FileListView")

  //noinspection TypeAnnotation
  class Actions {
    val changeDir = mockFunction[Dispatch, Option[String], String, FileListDirChangeAction]

    val actions = new MockFileListActions(
      changeDirMock = changeDir
    )
  }

  it should "dispatch action only once when mount but not when update" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val state1 = FileListState()
    val props1 = FileListProps(dispatch, actions.actions, state1, (7, 2), columns = 2)
    val state2 = FileListState.copy(state1)(isActive = true)
    val props2 = props1.copy(state = state2)
    val action = FileListDirChangeAction(
      Task("Changing dir", Future.successful(state1.currDir))
    )
    
    //then
    actions.changeDir.expects(dispatch, None, FileListItem.currDir.name).returning(action)
    dispatch.expects(action)
    
    //when
    val renderer = createTestRenderer(<(FileList())(^.wrapped := props1)())
    renderer.update(<(FileList())(^.wrapped := props2)()) //noop
    
    //cleanup
    renderer.unmount()

    action.task.result.toFuture.map(_ => Succeeded)
  }

  it should "focus item when onWheel and active" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = js.Array(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3"),
        FileListItem("item 4"),
        FileListItem("item 5")
      )),
      isActive = true
    ), (7, 3), columns = 2)

    val renderer = createTestRenderer(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.root, fileListViewComp).focusedIndex shouldBe 0

    def check(up: Boolean, offset: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = FileListState.copy(props.state)(offset = offset, index = index)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(offset, index, Set.empty))
      }
      
      //when
      findComponentProps(renderer.root, fileListViewComp).onWheel(up)
      renderer.update(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.root, fileListViewComp)
      res.focusedIndex shouldBe index
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

  it should "not focus item when onWheel and not active" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = js.Array(
        FileListItem("item 1"),
        FileListItem("item 2")
      ))
    ), (7, 3), columns = 2)

    val comp = testRender(<(FileList())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, fileListViewComp)
    viewProps.focusedIndex shouldBe -1

    //then
    dispatch.expects(*).never()
    
    //when
    viewProps.onWheel(false)
    viewProps.onWheel(true)
    
    Succeeded
  }

  it should "focus item when onClick" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = js.Array(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      isActive = true
    ), (7, 3), columns = 2)

    val renderer = createTestRenderer(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.root, fileListViewComp).focusedIndex shouldBe 0

    def check(clickIndex: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = FileListState.copy(props.state)(offset = 0, index = index)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(0, index, Set.empty))
      }

      //when
      findComponentProps(renderer.root, fileListViewComp).onClick(clickIndex)
      renderer.update(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.root, fileListViewComp)
      res.focusedIndex shouldBe index
    }

    //when & then
    check(clickIndex = 0, index = 0, changed = false) // first item in col 1
    check(clickIndex = 1, index = 1) // second item in col 1
    check(clickIndex = 2, index = 2) // first item in col 2
    check(clickIndex = 3, index = 2, changed = false) // last item in col 2
  }

  it should "focus and select item when onKeypress" in {
    //given
    val onKeypress = mockFunction[BlessedScreen, String, Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val items = js.Array(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3"),
      FileListItem("item 4"),
      FileListItem("item 5"),
      FileListItem("item 6"),
      FileListItem("item 7")
    )
    val rootProps = FileListProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = items),
      isActive = true
    ), (7, 3), columns = 2, onKeypress = onKeypress)
    val screen = js.Dynamic.literal().asInstanceOf[BlessedScreen]

    val renderer = createTestRenderer(<(FileList())(^.wrapped := rootProps)())
    findComponentProps(renderer.root, fileListViewComp).focusedIndex shouldBe 0
    
    def check(keyFull: String,
              items: List[String],
              offset: Int,
              index: Int,
              selected: js.Set[String],
              changed: Boolean = true,
              props: FileListProps = rootProps
             )(implicit pos: Position): Assertion = {

      val state = FileListState.copy(props.state)(offset = offset, index = index, selectedNames = selected)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(offset, index, selected.toSet))
      }
      
      //then
      onKeypress.expects(screen, keyFull)

      //when
      findComponentProps(renderer.root, fileListViewComp).onKeypress(screen, keyFull)
      renderer.update(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.root, fileListViewComp)
      val viewItems = items.map(name => FileListItem(name, isDir = name == FileListItem.up.name))
      assertFileListItems(res.items, viewItems)
      (res.focusedIndex, res.selectedNames.toSet) shouldBe ((index, selected.toSet))
    }

    //when & then
    check("unknown", List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty, changed = false)
    
    //when & then
    check("S-down",  List("item 1", "item 2", "item 3", "item 4"), 0, 1, js.Set("item 1"))
    check("S-down",  List("item 1", "item 2", "item 3", "item 4"), 0, 2, js.Set("item 1", "item 2"))
    check("down",    List("item 1", "item 2", "item 3", "item 4"), 0, 3, js.Set("item 1", "item 2"))
    check("down",    List("item 2", "item 3", "item 4", "item 5"), 1, 3, js.Set("item 1", "item 2"))
    check("S-down",  List("item 3", "item 4", "item 5", "item 6"), 2, 3, js.Set("item 1", "item 2", "item 5"))
    check("S-down",  List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6"))
    check("S-down",  List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6", "item 7"))
    check("S-down",  List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6"))
    check("S-down",  List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6", "item 7"))
    check("down",    List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 2, js.Set("item 1", "item 2", "item 5", "item 6"))
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 1, js.Set("item 1", "item 2", "item 5"))
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 0, js.Set("item 1", "item 2"))
    check("up",      List("item 3", "item 4", "item 5", "item 6"), 2, 0, js.Set("item 1", "item 2"))
    check("up",      List("item 2", "item 3", "item 4", "item 5"), 1, 0, js.Set("item 1", "item 2"))
    check("S-up",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set("item 1"))
    check("S-up",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)
    check("S-up",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set("item 1"))
    check("S-up",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)
    check("up",      List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty, changed = false)

    //when & then
    check("S-right", List("item 1", "item 2", "item 3", "item 4"), 0, 2, js.Set("item 1", "item 2"))
    check("right",   List("item 3", "item 4", "item 5", "item 6"), 2, 2, js.Set("item 1", "item 2"))
    check("S-right", List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6", "item 7"))
    check("right",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-left",  List("item 4", "item 5", "item 6", "item 7"), 3, 1, js.Set("item 1", "item 2", "item 5"))
    check("left",    List("item 2", "item 3", "item 4", "item 5"), 1, 1, js.Set("item 1", "item 2", "item 5"))
    check("S-left",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set("item 1", "item 2", "item 3", "item 5"))
    check("left",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set("item 1", "item 2", "item 3", "item 5"), changed = false)

    //when & then
    check("S-pagedown", List("item 1", "item 2", "item 3", "item 4"), 0, 3, js.Set("item 5"))
    check("S-pagedown", List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 4", "item 5", "item 6", "item 7"))
    check("pagedown",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 4", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-pageup",List("item 4", "item 5", "item 6", "item 7"), 3, 0, js.Set("item 4"))
    check("S-pageup",List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)
    check("pageup",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty, changed = false)

    //when & then
    check("end",     List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set.empty)
    check("end",     List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set.empty, changed = false)

    //when & then
    check("home",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)
    check("home",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty, changed = false)

    //when & then
    check("S-end",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 3", "item 4", "item 5", "item 6", "item 7"))
    check("S-end",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 3", "item 4", "item 5", "item 6"))
    check("S-end",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, js.Set("item 1", "item 2", "item 3", "item 4", "item 5", "item 6", "item 7"))

    //when & then
    check("S-home",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)
    check("S-home",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set("item 1"))
    check("S-home",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, js.Set.empty)

    //given
    val nonRootProps = rootProps.copy(state = FileListState.copy(rootProps.state)(
      currDir = FileListDir.copy(rootProps.state.currDir)(items = FileListItem.up +: items)
    ))
    renderer.update(<(FileList())(^.wrapped := nonRootProps)())
    findComponentProps(renderer.root, fileListViewComp).focusedIndex shouldBe 0

    //when & then
    check("S-down",  List("..", "item 1", "item 2", "item 3"), 0, 1, js.Set.empty, props = nonRootProps)
    check("S-down",  List("..", "item 1", "item 2", "item 3"), 0, 2, js.Set("item 1"), props = nonRootProps)
    check("up",      List("..", "item 1", "item 2", "item 3"), 0, 1, js.Set("item 1"), props = nonRootProps)
    check("S-up",    List("..", "item 1", "item 2", "item 3"), 0, 0, js.Set.empty, props = nonRootProps)
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(), (7, 2), columns = 2)
    val dirAction = FileListDirChangeAction(
      Task("Changing dir", Future.successful(props.state.currDir))
    )
    actions.changeDir.expects(dispatch, None, FileListItem.currDir.name).returning(dirAction)
    dispatch.expects(dirAction)

    //when
    val result = testRender(<(FileList())(^.wrapped := props)())

    //then
    dirAction.task.result.toFuture.map { _ =>
      assertFileList(result, props,
        viewItems = Nil,
        focusedIndex = -1,
        selectedNames = Set.empty
      )
    }
  }
  
  it should "render non-empty component and focus first item" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = js.Array(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      isActive = true
    ), (7, 2), columns = 2)

    //when
    val result = testRender(<(FileList())(^.wrapped := props)())

    //then
    assertFileList(result, props,
      viewItems = List(FileListItem("item 1"), FileListItem("item 2")),
      focusedIndex = 0,
      selectedNames = Set.empty
    )
  }
  
  it should "render non-empty component and focus last item" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListProps(dispatch, actions.actions, FileListState(
      index = 2,
      currDir = FileListDir("/", isRoot = true, items = js.Array(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      isActive = true
    ), (7, 2), columns = 2)

    //when
    val result = testRender(<(FileList())(^.wrapped := props)())

    //then
    assertFileList(result, props,
      viewItems = List(FileListItem("item 2"), FileListItem("item 3")),
      focusedIndex = 1,
      selectedNames = Set.empty
    )
  }
  
  private def assertFileList(result: TestInstance,
                             props: FileListProps,
                             viewItems: List[FileListItem],
                             focusedIndex: Int,
                             selectedNames: Set[Int]): Assertion = {
    
    assertTestComponent(result, fileListViewComp) {
      case FileListViewProps(resSize, columns, items, resFocusedIndex, resSelectedNames, _, _, _) =>
        resSize shouldBe props.size
        columns shouldBe props.columns
        assertFileListItems(items, viewItems)
        resFocusedIndex shouldBe focusedIndex
        resSelectedNames shouldBe selectedNames
    }
  }
}
