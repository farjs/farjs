package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.SortMode
import scommons.react.test.TestSpec

class FileListStateReducerSpec extends TestSpec {
  
  private val reduce = FileListStateReducer.apply _

  it should "set params when FileListParamsChangedAction" in {
    //given
    val state = FileListState(isActive = true)
    val action = FileListParamsChangedAction(
      offset = 1,
      index = 2,
      selectedNames = Set("test")
    )
    
    //when & then
    reduce(state, action) shouldBe {
      FileListState(
        offset = action.offset,
        index = action.index,
        selectedNames = action.selectedNames,
        isActive = true
      )
    }
  }
  
  it should "set sorted items when FileListDirChangedAction(root)" in {
    //given
    val state = FileListState(isActive = true)
    val currDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirChangedAction(FileListDir.curr, currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      FileListState(
        currDir = currDir.copy(items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty,
        isActive = true
      )
    }
  }
  
  it should "add .. to items and set index when FileListDirChangedAction(not root)" in {
    //given
    val stateDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = Seq.empty)
    val state = FileListState(currDir = stateDir, selectedNames = Set("test"))
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirChangedAction(FileListItem.up.name, currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      FileListState(
        index = 2,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set.empty
      )
    }
  }

  it should "update state and keep current item when FileListDirUpdatedAction" in {
    //given
    val state = FileListState().copy(
      offset = 1,
      index = 0,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1")
      )),
      selectedNames = Set("test", "dir 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      state.copy(
        offset = 0,
        index = 2,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("file 1")
        )),
        selectedNames = Set("dir 1")
      )
    }
  }

  it should "update state and keep current index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState().copy(
      offset = 1,
      index = 0,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1")
      )),
      selectedNames = Set("test", "file 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      state.copy(
        offset = 1,
        index = 0,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true)
        )),
        selectedNames = Set.empty
      )
    }
  }

  it should "update state and reset index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState(
      offset = 1,
      index = 1,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      )),
      selectedNames = Set("file 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = Seq.empty)
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      state.copy(
        offset = 0,
        index = 0,
        currDir = currDir.copy(items = List(
          FileListItem.up
        )),
        selectedNames = Set.empty
      )
    }
  }

  it should "update state and set default index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState(
      offset = 1,
      index = 1,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = true, items = Nil)
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = List(
      FileListItem("file 1"),
      FileListItem("Fixes"),
      FileListItem("Food", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    reduce(state, action) shouldBe {
      state.copy(
        offset = 0,
        index = 0,
        currDir = currDir.copy(items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("Food", isDir = true),
          FileListItem("file 1"),
          FileListItem("Fixes")
        )),
        selectedNames = Set.empty
      )
    }
  }

  it should "update state when FileListItemCreatedAction" in {
    //given
    val stateDir = FileListDir("/", isRoot = true, items = Seq.empty)
    val state = FileListState(offset = 1, currDir = stateDir, selectedNames = Set("test1"))
    val dir = "dir 2"
    val currDir = stateDir.copy(items = List(
      FileListItem("file 2"),
      FileListItem("File 1"),
      FileListItem(dir, isDir = true),
      FileListItem("Dir 1", isDir = true)
    ))
    val action = FileListItemCreatedAction(dir, currDir)

    //when & then
    reduce(state, action) shouldBe {
      state.copy(
        offset = 0,
        index = 1,
        currDir = currDir.copy(items = List(
          FileListItem("Dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("File 1"),
          FileListItem("file 2")
        )),
        selectedNames = Set("test1")
      )
    }
  }

  it should "keep current sate if item not found when FileListItemCreatedAction" in {
    //given
    val stateDir = FileListDir("/", isRoot = true, items = List(
      FileListItem("Dir 1", isDir = true),
      FileListItem("dir 2", isDir = true),
      FileListItem("File 1"),
      FileListItem("file 2")
    ))
    val state = FileListState(offset = 1, currDir = stateDir, selectedNames = Set("test1"))
    val action = FileListItemCreatedAction("non-existing", stateDir)

    //when & then
    reduce(state, action) shouldBe state
  }

  it should "update state when FileListSortByAction" in {
    //given
    val state = FileListState(offset = 1, currDir = FileListDir("/", isRoot = false, items = List(
      FileListItem.up,
      FileListItem("file 2"),
      FileListItem("File 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("Dir 1", isDir = true)
    )))
    val action = FileListSortByAction(SortMode.Name)

    //when & then
    reduce(state, action) shouldBe {
      state.copy(offset = 0, index = 3, currDir = state.currDir.copy(items = List(
        FileListItem.up,
        FileListItem("dir 2", isDir = true),
        FileListItem("Dir 1", isDir = true),
        FileListItem("file 2"),
        FileListItem("File 1")
      )), sortAscending = false)
    }
  }

  it should "keep current offset/index if item not found when FileListSortByAction" in {
    //given
    val state = FileListState(offset = 1, currDir = FileListDir("/", isRoot = true, items = Nil))
    val action = FileListSortByAction(SortMode.Name)

    //when & then
    reduce(state, action) shouldBe {
      state.copy(sortAscending = false)
    }
  }

  it should "update state when FileListDiskSpaceUpdatedAction" in {
    //given
    val state = FileListState()
    val action = FileListDiskSpaceUpdatedAction(123.45)
    state.diskSpace shouldBe None

    //when & then
    reduce(state, action) shouldBe state.copy(
      diskSpace = Some(123.45)
    )
  }
}
