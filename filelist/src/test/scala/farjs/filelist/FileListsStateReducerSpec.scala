package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.react.test.TestSpec

class FileListsStateReducerSpec extends TestSpec {

  private val reduce = FileListsStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FileListsState()
  }
  
  it should "set isActive when FileListActivateAction" in {
    //given
    val state = FileListsState()
    val action = FileListActivateAction(isRight = false)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(
        left = FileListState(isActive = true),
        right = FileListState(isRight = true)
      )
    }
    //when & then
    reduce(Some(state), action.copy(isRight = true)) shouldBe {
      state.copy(
        left = FileListState(),
        right = FileListState(isRight = true, isActive = true)
      )
    }
  }
  
  it should "set params when FileListParamsChangedAction" in {
    //given
    val state = FileListsState()
    val action = FileListParamsChangedAction(
      isRight = false,
      offset = 1,
      index = 2,
      selectedNames = Set("test")
    )
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(left = FileListState(
        offset = action.offset,
        index = action.index,
        selectedNames = action.selectedNames,
        isActive = true
      ))
    }
    //when & then
    reduce(Some(state), action.copy(isRight = true)) shouldBe {
      state.copy(right = FileListState(
        offset = action.offset,
        index = action.index,
        selectedNames = action.selectedNames,
        isRight = true
      ))
    }
  }
  
  it should "set sorted items when FileListDirChangedAction(root)" in {
    //given
    val state = FileListsState()
    val currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = false, FileListDir.curr, currDir)) shouldBe {
      state.copy(left = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty,
        isActive = true
      ))
    }
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = true, FileListDir.curr, currDir)) shouldBe {
      state.copy(right = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty,
        isRight = true
      ))
    }
  }
  
  it should "add .. to items and set index when FileListDirChangedAction(not root)" in {
    //given
    val stateDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = Seq.empty)
    val state = FileListsState(
      left = FileListState(currDir = stateDir, selectedNames = Set("test")),
      right = FileListState(currDir = stateDir, selectedNames = Set("test"), isRight = true)
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = false, FileListItem.up.name, currDir)) shouldBe {
      state.copy(left = FileListState(
        index = 2,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty
      ))
    }
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = true, FileListItem.up.name, currDir)) shouldBe {
      state.copy(right = FileListState(
        index = 2,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty,
        isRight = true
      ))
    }
  }

  it should "update FileListState and keep current item when FileListDirUpdatedAction" in {
    //given
    val state = FileListsState(
      left = FileListState().copy(
        offset = 1,
        index = 0,
        currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
          FileListItem.up,
          FileListItem("file 1")
        )),
        selectedNames = Set("test")
      )
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    ))
    
    //when & then
    reduce(Some(state), FileListDirUpdatedAction(isRight = false, currDir)) shouldBe {
      state.copy(left = state.left.copy(
        offset = 0,
        index = 2,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("file 1")
        )),
        selectedNames = Set("test")
      ))
    }
  }

  it should "update FileListState and keep current index when FileListDirUpdatedAction" in {
    //given
    val state = FileListsState(
      left = FileListState().copy(
        offset = 1,
        index = 0,
        currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
          FileListItem.up,
          FileListItem("file 1")
        )),
        selectedNames = Set("test")
      )
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("dir 1", isDir = true)
    ))
    
    //when & then
    reduce(Some(state), FileListDirUpdatedAction(isRight = false, currDir)) shouldBe {
      state.copy(left = state.left.copy(
        offset = 0,
        index = 1,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true)
        )),
        selectedNames = Set("test")
      ))
    }
  }

  it should "update FileListState and reset index when FileListDirUpdatedAction" in {
    //given
    val state = FileListsState(
      left = FileListState(
        offset = 1,
        index = 1,
        currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
          FileListItem.up,
          FileListItem("file 1"),
          FileListItem("dir 1", isDir = true)
        )),
        selectedNames = Set("test")
      )
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = Seq.empty)
    
    //when & then
    reduce(Some(state), FileListDirUpdatedAction(isRight = false, currDir)) shouldBe {
      state.copy(left = state.left.copy(
        offset = 0,
        index = 0,
        currDir = currDir.copy(items = List(
          FileListItem.up
        )),
        selectedNames = Set("test")
      ))
    }
  }

  it should "update FileListState and set default index when FileListDirUpdatedAction" in {
    //given
    val state = FileListsState(
      left = FileListState(
        offset = 1,
        index = 1,
        currDir = FileListDir("/root/sub-dir/dir 2", isRoot = true, items = Nil),
        selectedNames = Set("test")
      )
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = Seq.empty)
    
    //when & then
    reduce(Some(state), FileListDirUpdatedAction(isRight = false, currDir)) shouldBe {
      state.copy(left = state.left.copy(
        offset = 0,
        index = 0,
        currDir = currDir.copy(items = List(
          FileListItem.up
        )),
        selectedNames = Set("test")
      ))
    }
  }

  it should "update FileListState when FileListDirCreatedAction" in {
    //given
    val stateDir = FileListDir("/", isRoot = true, items = Seq.empty)
    val state = FileListsState(
      left = FileListState(offset = 1, currDir = stateDir, selectedNames = Set("test1")),
      right = FileListState(offset = 2, currDir = stateDir, selectedNames = Set("test2"), isRight = true)
    )
    val dir = "dir 2"
    val currDir = stateDir.copy(items = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem(dir, isDir = true),
      FileListItem("dir 1", isDir = true)
    ))

    //when & then
    reduce(Some(state), FileListDirCreatedAction(isRight = false, dir, currDir)) shouldBe {
      state.copy(left = state.left.copy(
        offset = 0,
        index = 1,
        currDir = currDir.copy(items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set("test1")
      ))
    }
    //when & then
    reduce(Some(state), FileListDirCreatedAction(isRight = true, dir, currDir)) shouldBe {
      state.copy(right = state.right.copy(
        offset = 0,
        index = 1,
        currDir = currDir.copy(items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set("test2"),
        isRight = true
      ))
    }
  }

  it should "update FileListState when FileListItemsDeletedAction(currItem)" in {
    //given
    val currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 1"),
      FileListItem("file 2")
    ))
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(currDir = currDir),
        right = state.right.copy(index = 1, currDir = currDir)
      )
    }

    //when & then
    reduce(Some(state), FileListItemsDeletedAction(isRight = false)) shouldBe {
      state.copy(left = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("file 2")
        )),
        isActive = true
      ))
    }
    //when & then
    reduce(Some(state), FileListItemsDeletedAction(isRight = true)) shouldBe {
      state.copy(right = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("file 1")
        )),
        isRight = true
      ))
    }
  }
  
  it should "update FileListState when FileListItemsDeletedAction(selectedItems)" in {
    //given
    val currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 0"),
      FileListItem("file 1"),
      FileListItem("file 2"),
      FileListItem("file 3"),
      FileListItem("file 4")
    ))
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(index = 2, currDir = currDir, selectedNames = Set("file 1", "file 2")),
        right = state.right.copy(offset = 3, currDir = currDir, selectedNames = Set("file 3", "file 4"))
      )
    }

    //when & then
    reduce(Some(state), FileListItemsDeletedAction(isRight = false)) shouldBe {
      state.copy(left = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("file 0"),
          FileListItem("file 3"),
          FileListItem("file 4")
        )),
        index = 1,
        selectedNames = Set.empty,
        isActive = true
      ))
    }
    //when & then
    reduce(Some(state), FileListItemsDeletedAction(isRight = true)) shouldBe {
      state.copy(right = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("file 0"),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        offset = 2,
        selectedNames = Set.empty,
        isRight = true
      ))
    }
  }
  
  it should "update state when FileListItemsViewedAction" in {
    //given
    val currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("dir 0"),
      FileListItem("dir 1"),
      FileListItem("file 3", size = 3)
    ))
    val state = {
      val state = FileListsState()
      state.copy(
        left = state.left.copy(index = 1, currDir = currDir)
      )
    }

    //when & then
    reduce(Some(state), FileListItemsViewedAction(isRight = false, Map(
      "dir 1" -> 123,
      "file 1" -> 10
    ))) shouldBe {
      state.copy(left = FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("dir 0"),
          FileListItem("dir 1", size = 123),
          FileListItem("file 3", size = 3)
        )),
        index = 1,
        selectedNames = Set.empty,
        isActive = true
      ))
    }
  }
}
