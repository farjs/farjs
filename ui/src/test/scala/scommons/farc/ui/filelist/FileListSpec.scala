package scommons.farc.ui.filelist

import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import scommons.farc.api.filelist._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test.BaseTestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.concurrent.Future

class FileListSpec extends AsyncTestSpec with BaseTestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "call api and onStateChanged when mount but not when update" in {
    //given
    val api = mock[FileListApi]
    val onStateChanged = mockFunction[FileListState, Unit]
    val props1 = FileListProps(api, (7, 2), columns = 2, FileListState(), onStateChanged = onStateChanged)
    val state1 = FileListState(items = List(FileListItem("item 1")))
    val root = "/"
    val future1 = Future.successful(root)
    val future2 = Future.successful(state1.items)
    val state2 = state1.copy(currDir = "/changed", items = List(FileListItem("item 2")))
    val props2 = props1.copy(state = state2)
    
    //then
    (api.rootDir _).expects().returning(root).twice()
    (api.changeDir _).expects(root).returning(future1)
    (api.listFiles _).expects().returning(future2)
    onStateChanged.expects(state1.copy(currDir = root))
    
    //when
    val renderer = createTestRenderer(<(FileList())(^.wrapped := props1)())
    renderer.update(<(FileList())(^.wrapped := props2)()) //noop
    
    //cleanup
    renderer.unmount()

    Future.sequence(List(future1, future2)).map(_ => Succeeded)
  }

  it should "call api and onStateChanged when onKeypress(enter)" in {
    //given
    val api = mock[FileListApi]
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps(api, (7, 3), columns = 2, FileListState(
      currDir = "/",
      items = List(
        FileListItem("dir 1", isDir = true),
        FileListItem("dir 2", isDir = true),
        FileListItem("dir 3", isDir = true),
        FileListItem("dir 4", isDir = true),
        FileListItem("dir 5", isDir = true),
        FileListItem("dir 6", isDir = true),
        FileListItem("file 7")
      )
    ), onStateChanged)

    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def prepare(offset: Int, index: Int, currDir: String, items: Seq[FileListItem]): Future[Assertion] = Future {
      renderer.render(<(FileList())(^.wrapped := props.copy(state = props.state.copy(
        offset = offset,
        index = index,
        currDir = currDir,
        items = items
      )))())
      
      Succeeded
    }

    def check(keyFull: String,
              pressItem: String,
              currDir: String,
              items: List[String],
              offset: Int,
              index: Int,
              changed: Boolean = true
             )(implicit pos: Position): Future[Assertion] = {

      val state = props.state.copy(offset = offset, index = index, currDir = currDir,
        items = if (currDir == "/") props.state.items else FileListItem.up +: props.state.items
      )
      val checkF =
        if (changed) {
          val future1 = Future.successful(currDir)
          val future2 = Future.successful(props.state.items)

          //then
          (api.rootDir _).expects().returning("/")
          (api.changeDir _).expects(pressItem).returning(future1)
          (api.listFiles _).expects().returning(future2)
          onStateChanged.expects(state)

          Future.sequence(List(future1, future2)).map(_ => Succeeded)
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
        (res.items, res.focusedIndex) shouldBe ((viewItems, index))
      }.flatMap(_ => checkF)
    }

    Future.sequence(List(
      //when & then
      check("unknown", "", "/", List("dir 1", "dir 2", "dir 3", "dir 4"), 0, 0, changed = false),
      
      check("enter", "dir 1", "/dir 1", List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      check("enter", "..",    "/",      List("dir 1", "dir 2", "dir 3", "dir 4"), 0, 0),
      
      prepare(3, 3, "/", props.state.items),
      check("enter", "file 7","/",      List("dir 4", "dir 5", "dir 6", "file 7"), 3, 3, changed = false),
      
      prepare(0, 1, "/", props.state.items),
      check("enter", "dir 2", "/dir 2", List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      
      prepare(0, 3, "/dir 2", FileListItem.up +: props.state.items),
      check("enter", "dir 3", "/dir 2/dir 3", List("..", "dir 1", "dir 2", "dir 3"), 0, 0),
      check("enter", "..",    "/dir 2",       List("..", "dir 1", "dir 2", "dir 3"), 0, 3),
      
      prepare(0, 0, "/dir 2", FileListItem.up +: props.state.items),
      check("enter", "..",    "/",            List("dir 1", "dir 2", "dir 3", "dir 4"), 0, 1)
      
    )).map(_ => Succeeded)
  }

  it should "focus item when onWheelup/onWheeldown" in {
    //given
    val api = mock[FileListApi]
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps(api, (7, 3), columns = 2, FileListState(items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3"),
      FileListItem("item 4"),
      FileListItem("item 5")
    )), onStateChanged = onStateChanged)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(up: Boolean, offset: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = props.state.copy(offset = offset, index = index)
      if (changed) {
        //then
        onStateChanged.expects(state)
      }
      
      //when
      if (up) findComponentProps(renderer.getRenderOutput(), FileListView).onWheelUp()
      else findComponentProps(renderer.getRenderOutput(), FileListView).onWheelDown()

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

  it should "focus item when onClick" in {
    //given
    val api = mock[FileListApi]
    val onStateChanged = mockFunction[FileListState, Unit]
    val props = FileListProps(api, (7, 3), columns = 2, FileListState(items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3")
    )), onStateChanged = onStateChanged)
    val renderer = createRenderer()
    renderer.render(<(FileList())(^.wrapped := props)())
    findComponentProps(renderer.getRenderOutput(), FileListView).focusedIndex shouldBe 0

    def check(clickIndex: Int, index: Int, changed: Boolean = true)(implicit pos: Position): Assertion = {
      val state = props.state.copy(offset = 0, index = index)
      if (changed) {
        //then
        onStateChanged.expects(state)
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
    val api = mock[FileListApi]
    val onStateChanged = mockFunction[FileListState, Unit]
    val items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3"),
      FileListItem("item 4"),
      FileListItem("item 5"),
      FileListItem("item 6"),
      FileListItem("item 7")
    )
    val rootProps = FileListProps(api, (7, 3), columns = 2, FileListState(items = items), onStateChanged)
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
        onStateChanged.expects(state)
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
    val nonRootProps = rootProps.copy(state = rootProps.state.copy(items = FileListItem.up +: items))
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
    val api = mock[FileListApi]
    val props = FileListProps(api, (7, 2), columns = 2, FileListState(), _ => ())
    val comp = <(FileList())(^.wrapped := props)()

    //when
    val result = shallowRender(comp)

    //then
    assertFileList(result, props,
      viewItems = Nil,
      focusedIndex = 0,
      selectedNames = Set.empty
    )
  }
  
  it should "render non-empty component" in {
    //given
    val api = mock[FileListApi]
    val props = FileListProps(api, (7, 2), columns = 2, FileListState(items = List(
      FileListItem("item 1"),
      FileListItem("item 2"),
      FileListItem("item 3")
    )), _ => ())
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
