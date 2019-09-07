package scommons.farc.ui.filelist

import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.farc.ui.filelist.popups.FileListPopupsActions
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.blessed._
import scommons.react.redux.task.FutureTask
import scommons.react.test.BaseTestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.concurrent.Future
import scala.scalajs.js

class FileListSpec extends AsyncTestSpec with BaseTestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "dispatch FileListHelpAction when F1" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    )
    val props = FileListProps(dispatch, actions, state, (5, 5), columns = 2)
    val comp = shallowRender(<(FileList())(^.wrapped := props)())

    //then
    dispatch.expects(FileListPopupsActions.FileListHelpAction(show = true))
    
    //when
    findComponentProps(comp, FileListView).onKeypress("f1")
    
    Succeeded
  }
  
  it should "emit keypress (Ctrl+C) event when F10" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    )
    val props = FileListProps(dispatch, actions, state, (5, 5), columns = 2)
    val comp = shallowRender(<(FileList())(^.wrapped := props)())
    
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    //then
    onKey.expects("c", true, false, false)
    
    //when
    findComponentProps(comp, FileListView).onKeypress("f10")

    //cleanup
    process.stdin.removeListener("keypress", listener)
    Succeeded
  }
  
  it should "dispatch action only once when mount but not when update" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val state1 = FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(FileListItem("item 1")))
    )
    val props1 = FileListProps(dispatch, actions, state1, (7, 2), columns = 2)
    val state2 = state1.copy(
      currDir = FileListDir("/changed", isRoot = false, items = List(FileListItem("item 2")))
    )
    val props2 = props1.copy(state = state2)
    val action = FileListDirChangeAction(
      FutureTask("Changing dir", Future.successful(state1.currDir))
    )
    
    //then
    (actions.changeDir _).expects(dispatch, state1.isRight, None, FileListDir.curr).returning(action)
    dispatch.expects(action)
    
    //when
    val renderer = createTestRenderer(<(FileList())(^.wrapped := props1)())
    renderer.update(<(FileList())(^.wrapped := props2)()) //noop
    
    //cleanup
    renderer.unmount()

    action.task.future.map(_ => Succeeded)
  }

  it should "dispatch action when onKeypress(enter)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("dir 1", isDir = true),
        FileListItem("dir 2", isDir = true),
        FileListItem("dir 3", isDir = true),
        FileListItem("dir 4", isDir = true),
        FileListItem("dir 5", isDir = true),
        FileListItem("dir 6", isDir = true),
        FileListItem("file 7")
      )),
      isActive = true
    ), (7, 3), columns = 2)

    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def prepare(offset: Int, index: Int, currDir: String, items: Seq[FileListItem]): Future[Assertion] = Future {
      renderer.render(<(FileList())(^.wrapped := props.copy(state = props.state.copy(
        offset = offset,
        index = index,
        currDir = FileListDir(currDir, currDir == "/", items = items)
      )))())

      Succeeded
    }

    def check(keyFull: String,
              parent: String,
              pressItem: String,
              items: List[String],
              offset: Int,
              index: Int,
              changed: Boolean = true
             )(implicit pos: Position): Future[Assertion] = {

      val currDirPath =
        if (changed) {
          if (pressItem == FileListItem.up.name) {
            val index = parent.lastIndexOf('/')
            parent.take(if (index > 0) index else 1)
          }
          else if (parent == "/") s"$parent$pressItem"
          else s"$parent/$pressItem"
        }
        else parent
      
      val isRoot = currDirPath == "/"
      val currDir = FileListDir(currDirPath, isRoot, items =
        if (isRoot) props.state.currDir.items
        else FileListItem.up +: props.state.currDir.items
      )
      val state = props.state.copy(offset = 0, index = offset + index, currDir = currDir)
      val checkF =
        if (changed) {
          val action = FileListDirChangeAction(
            FutureTask("Changing dir", Future.successful(currDir))
          )

          //then
          (actions.changeDir _).expects(dispatch, state.isRight, Some(parent), pressItem).returning(action)
          dispatch.expects(action)

          action.task.future.map(_ => Succeeded)
        }
        else Future.successful(Succeeded)

      Future {
        //when
        findComponentProps(renderer.getRenderOutput(), FileListView).onKeypress(keyFull)
        renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

        //then
        val res = findComponentProps(renderer.getRenderOutput(), FileListView)
        val viewItems = items.map(name => FileListItem(
          name = name,
          isDir = name == FileListItem.up.name || name.startsWith("dir")
        ))
        (res.items.toList, res.focusedIndex) shouldBe ((viewItems, index))
      }.flatMap(_ => checkF)
    }

    Future.sequence(List(
      //when & then
      check("unknown", "/",    "123",         List("dir 1", "dir 2", "dir 3", "dir 4"), 0, 0, changed = false),
      
      check("enter", "/",      "dir 1",       List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      check("enter", "/dir 1", "..",          List("dir 1", "dir 2", "dir 3", "dir 4"), 0, 0),
      
      prepare(3, 3, "/", props.state.currDir.items),
      check("enter", "/",      "file 7",      List("dir 5", "dir 6", "file 7"), 4, 2, changed = false),
      
      prepare(3, 2, "/", props.state.currDir.items),
      check("enter", "/",      "dir 6",       List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      
      prepare(3, 1, "/dir 6", FileListItem.up +: props.state.currDir.items),
      check("enter", "/dir 6",       "dir 4", List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      check("enter", "/dir 6/dir 4", "..",    List("dir 4", "dir 5", "dir 6", "file 7"), 4, 0),
      
      prepare(0, 0, "/dir 6", FileListItem.up +: props.state.currDir.items),
      check("enter", "/dir 6",       "..",    List("dir 5", "dir 6", "file 7"), 4, 1)
      
    )).map(_ => Succeeded)
  }

  it should "dispatch action when onActivate" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("item 1"),
        FileListItem("item 2")
      ))
    ), (7, 3), columns = 2)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe -1

    def check(active: Boolean, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = props.state.copy(isActive = active)
      if (changed) {
        //then
        dispatch.expects(FileListActivateAction(isRight = state.isRight))
      }
      
      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onActivate()
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.getRenderOutput(), FileListView)
      res.focusedIndex shouldBe {
        if (active) 0
        else -1
      }
    }
    
    //when & then
    check(active = true)
    check(active = true, changed = false)
    
    //when & then
    check(active = false)
    check(active = false, changed = false)
  }

  it should "focus item when onWheel and active" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3"),
        FileListItem("item 4"),
        FileListItem("item 5")
      )),
      isActive = true
    ), (7, 3), columns = 2)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(up: Boolean, offset: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = props.state.copy(offset = offset, index = index)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(state.isRight, offset, index, Set.empty))
      }
      
      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onWheel(up)
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.getRenderOutput(), FileListView)
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
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("item 1"),
        FileListItem("item 2")
      ))
    ), (7, 3), columns = 2)
    val comp = shallowRender(<(FileList())(^.wrapped := props)())
    val viewProps = findComponentProps(comp, FileListView)
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
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      isActive = true
    ), (7, 3), columns = 2)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(clickIndex: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = props.state.copy(offset = 0, index = index)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(state.isRight, 0, index, Set.empty))
      }

      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onClick(clickIndex)
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.getRenderOutput(), FileListView)
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
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3"),
      FileListItem("item 4"),
      FileListItem("item 5"),
      FileListItem("item 6"),
      FileListItem("item 7")
    )
    val rootProps = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = items),
      isActive = true
    ), (7, 3), columns = 2)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := rootProps)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0
    
    def check(keyFull: String,
              items: List[String],
              offset: Int,
              index: Int,
              selected: Set[String],
              changed: Boolean = true,
              props: FileListProps = rootProps
             )(implicit pos: Position): Assertion = {

      val state = props.state.copy(offset = offset, index = index, selectedNames = selected)
      if (changed) {
        //then
        dispatch.expects(FileListParamsChangedAction(state.isRight, offset, index, selected))
      }
      
      //when
      findComponentProps(renderer.getRenderOutput(), FileListView).onKeypress(keyFull)
      renderer.render(<(FileList())(^.wrapped := props.copy(state = state))())

      //then
      val res = findComponentProps(renderer.getRenderOutput(), FileListView)
      val viewItems = items.map(name => FileListItem(name, isDir = name == FileListItem.up.name))
      (res.items, res.focusedIndex, res.selectedNames) shouldBe ((viewItems, index, selected))
    }
    
    //when & then
    check("unknown", List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty, changed = false)
    
    //when & then
    check("S-down",  List("item 1", "item 2", "item 3", "item 4"), 0, 1, Set("item 1"))
    check("S-down",  List("item 1", "item 2", "item 3", "item 4"), 0, 2, Set("item 1", "item 2"))
    check("down",    List("item 1", "item 2", "item 3", "item 4"), 0, 3, Set("item 1", "item 2"))
    check("down",    List("item 2", "item 3", "item 4", "item 5"), 1, 3, Set("item 1", "item 2"))
    check("S-down",  List("item 3", "item 4", "item 5", "item 6"), 2, 3, Set("item 1", "item 2", "item 5"))
    check("S-down",  List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 5", "item 6", "item 7"))
    check("down",    List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 2, Set("item 1", "item 2", "item 5", "item 6"))
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 1, Set("item 1", "item 2", "item 5"))
    check("S-up",    List("item 4", "item 5", "item 6", "item 7"), 3, 0, Set("item 1", "item 2"))
    check("up",      List("item 3", "item 4", "item 5", "item 6"), 2, 0, Set("item 1", "item 2"))
    check("up",      List("item 2", "item 3", "item 4", "item 5"), 1, 0, Set("item 1", "item 2"))
    check("S-up",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty)
    check("up",      List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty, changed = false)

    //when & then
    check("S-right", List("item 1", "item 2", "item 3", "item 4"), 0, 2, Set("item 1", "item 2"))
    check("right",   List("item 3", "item 4", "item 5", "item 6"), 2, 2, Set("item 1", "item 2"))
    check("S-right", List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 5", "item 6", "item 7"))
    check("right",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-left",  List("item 4", "item 5", "item 6", "item 7"), 3, 1, Set("item 1", "item 2", "item 5"))
    check("left",    List("item 2", "item 3", "item 4", "item 5"), 1, 1, Set("item 1", "item 2", "item 5"))
    check("S-left",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set("item 1", "item 2", "item 3", "item 5"))
    check("left",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set("item 1", "item 2", "item 3", "item 5"), changed = false)

    //when & then
    check("S-pagedown", List("item 1", "item 2", "item 3", "item 4"), 0, 3, Set("item 5"))
    check("S-pagedown", List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 4", "item 5", "item 6", "item 7"))
    check("pagedown",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 4", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-pageup",List("item 4", "item 5", "item 6", "item 7"), 3, 0, Set("item 4"))
    check("S-pageup",List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty)
    check("pageup",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty, changed = false)

    //when & then
    check("end",     List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set.empty)
    check("end",     List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set.empty, changed = false)

    //when & then
    check("home",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty)
    check("home",    List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty, changed = false)
    
    //when & then
    check("S-end",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 3", "item 4", "item 5", "item 6", "item 7"))
    check("S-end",   List("item 4", "item 5", "item 6", "item 7"), 3, 3, Set("item 1", "item 2", "item 3", "item 4", "item 5", "item 6", "item 7"), changed = false)

    //when & then
    check("S-home",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty)
    check("S-home",  List("item 1", "item 2", "item 3", "item 4"), 0, 0, Set.empty, changed = false)

    //given
    val nonRootProps = rootProps.copy(state = rootProps.state.copy(
      currDir = rootProps.state.currDir.copy(items = FileListItem.up +: items)
    ))
    renderer.render(<(FileList())(^.wrapped := nonRootProps)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    //when & then
    check("S-down",  List("..", "item 1", "item 2", "item 3"), 0, 1, Set.empty, props = nonRootProps)
    check("S-down",  List("..", "item 1", "item 2", "item 3"), 0, 2, Set("item 1"), props = nonRootProps)
    check("up",      List("..", "item 1", "item 2", "item 3"), 0, 1, Set("item 1"), props = nonRootProps)
    check("S-up",    List("..", "item 1", "item 2", "item 3"), 0, 0, Set.empty, props = nonRootProps)
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(), (7, 2), columns = 2)
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = Nil,
      focusedIndex = -1,
      selectedNames = Set.empty
    )
  }
  
  it should "render non-empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListProps(dispatch, actions, FileListState(
      currDir = FileListDir("/", isRoot = true, items = List(
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      isActive = true
    ), (7, 2), columns = 2)
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = List(FileListItem("item 1"), FileListItem("item 2")),
      focusedIndex = 0,
      selectedNames = Set.empty
    )
  }
  
  private def assertFileList(result: ShallowInstance,
                             props: FileListProps,
                             viewItems: List[FileListItem],
                             focusedIndex: Int,
                             selectedNames: Set[Int]): Assertion = {
    
    assertComponent(result, FileListView) {
      case FileListViewProps(resSize, columns, items, resFocusedIndex, resSelectedNames, _, _, _, _) =>
        resSize shouldBe props.size
        columns shouldBe props.columns
        items shouldBe viewItems
        resFocusedIndex shouldBe focusedIndex
        resSelectedNames shouldBe selectedNames
    }
  }
}
