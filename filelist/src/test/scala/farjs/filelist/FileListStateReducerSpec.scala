package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.FileListStateSpec.assertFileListState
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.{FileListSort, SortMode}
import scommons.react.test.TestSpec

import scala.scalajs.js

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
    
    //when
    val result = reduce(state, action)
    
    //then
    assertFileListState(result, state.copy(
      offset = action.offset,
      index = action.index,
      selectedNames = action.selectedNames,
      isActive = true
    ))
  }
  
  it should "set sorted items when FileListDirChangedAction(root)" in {
    //given
    val state = FileListState(isActive = true)
    val currDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirChangedAction(FileListItem.currDir.name, currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem("dir 1", isDir = true),
        FileListItem("dir 2", isDir = true),
        FileListItem("file 1"),
        FileListItem("file 2")
      ))
      assertFileListState(resState, FileListState(
        currDir = expectedCurrDir,
        selectedNames = Set.empty,
        isActive = true
      ))
    }
  }
  
  it should "add .. to items and set index when FileListDirChangedAction(not root)" in {
    //given
    val stateDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = js.Array())
    val state = FileListState(currDir = stateDir, selectedNames = Set("test"))
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = js.Array(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirChangedAction(FileListItem.up.name, currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true),
        FileListItem("dir 2", isDir = true),
        FileListItem("file 1"),
        FileListItem("file 2")
      ))
      assertFileListState(resState, FileListState(
        index = 2,
        currDir = expectedCurrDir,
        selectedNames = Set.empty
      ))
    }
  }

  it should "update state and keep current item when FileListDirUpdatedAction" in {
    //given
    val state = FileListState().copy(
      offset = 1,
      index = 0,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      )),
      selectedNames = Set("test", "dir 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = js.Array(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true),
        FileListItem("file 1")
      ))
      assertFileListState(resState, state.copy(
        offset = 0,
        index = 2,
        currDir = expectedCurrDir,
        selectedNames = Set("dir 1")
      ))
    }
  }

  it should "update state and keep current index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState().copy(
      offset = 1,
      index = 0,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1")
      )),
      selectedNames = Set("test", "file 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
      assertFileListState(resState, state.copy(
        offset = 1,
        index = 0,
        currDir = expectedCurrDir,
        selectedNames = Set.empty
      ))
    }
  }

  it should "update state and reset index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState(
      offset = 1,
      index = 1,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("file 1"),
        FileListItem("dir 1", isDir = true)
      )),
      selectedNames = Set("file 1")
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = js.Array())
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem.up
      ))
      assertFileListState(resState, state.copy(
        offset = 0,
        index = 0,
        currDir = expectedCurrDir,
        selectedNames = Set.empty
      ))
    }
  }

  it should "update state and set default index when FileListDirUpdatedAction" in {
    //given
    val state = FileListState(
      offset = 1,
      index = 1,
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = true, items = js.Array())
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false, items = js.Array(
      FileListItem("file 1"),
      FileListItem("Fixes"),
      FileListItem("Food", isDir = true),
      FileListItem("dir 1", isDir = true)
    ))
    val action = FileListDirUpdatedAction(currDir)
    
    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem.up,
        FileListItem("dir 1", isDir = true),
        FileListItem("Food", isDir = true),
        FileListItem("file 1"),
        FileListItem("Fixes")
      ))
      assertFileListState(resState, state.copy(
        offset = 0,
        index = 0,
        currDir = expectedCurrDir,
        selectedNames = Set.empty
      ))
    }
  }

  it should "update state when FileListItemCreatedAction" in {
    //given
    val stateDir = FileListDir("/", isRoot = true, items = js.Array())
    val state = FileListState(offset = 1, currDir = stateDir, selectedNames = Set("test1"))
    val dir = "dir 2"
    val currDir = FileListDir.copy(stateDir)(items = js.Array(
      FileListItem("file 2"),
      FileListItem("File 1"),
      FileListItem(dir, isDir = true),
      FileListItem("Dir 1", isDir = true)
    ))
    val action = FileListItemCreatedAction(dir, currDir)

    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(currDir)(items = js.Array(
        FileListItem("Dir 1", isDir = true),
        FileListItem("dir 2", isDir = true),
        FileListItem("File 1"),
        FileListItem("file 2")
      ))
      assertFileListState(resState, state.copy(
        offset = 0,
        index = 1,
        currDir = expectedCurrDir,
        selectedNames = Set("test1")
      ))
    }
  }

  it should "keep current sate if item not found when FileListItemCreatedAction" in {
    //given
    val stateDir = FileListDir("/", isRoot = true, items = js.Array(
      FileListItem("Dir 1", isDir = true),
      FileListItem("dir 2", isDir = true),
      FileListItem("File 1"),
      FileListItem("file 2")
    ))
    val state = FileListState(offset = 1, currDir = stateDir, selectedNames = Set("test1"))
    val action = FileListItemCreatedAction("non-existing", stateDir)

    //when & then
    inside(reduce(state, action)) { case resState =>
      assertFileListState(resState, state)
    }
  }

  it should "update state when FileListSortAction" in {
    //given
    val state = FileListState(offset = 1, currDir = FileListDir("/", isRoot = false, items = js.Array(
      FileListItem.up,
      FileListItem("file 2"),
      FileListItem("File 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("Dir 1", isDir = true)
    )))
    val action = FileListSortAction(SortMode.Name)

    //when & then
    inside(reduce(state, action)) { case resState =>
      val expectedCurrDir = FileListDir.copy(state.currDir)(items = js.Array(
        FileListItem.up,
        FileListItem("dir 2", isDir = true),
        FileListItem("Dir 1", isDir = true),
        FileListItem("file 2"),
        FileListItem("File 1")
      ))
      assertFileListState(resState, state.copy(
        offset = 0,
        index = 3,
        currDir = expectedCurrDir,
        sort = FileListSort.copy(state.sort)(asc = false)
      ))
    }
  }

  it should "keep current offset/index if item not found when FileListSortAction" in {
    //given
    val state = FileListState(offset = 1, currDir = FileListDir("/", isRoot = true, items = js.Array()))
    val action = FileListSortAction(SortMode.Name)

    //when & then
    inside(reduce(state, action)) { case resState =>
      assertFileListState(resState, state.copy(
        sort = FileListSort.copy(state.sort)(asc = false)
      ))
    } 
  }

  it should "update state when FileListDiskSpaceUpdatedAction" in {
    //given
    val state = FileListState()
    val action = FileListDiskSpaceUpdatedAction(123.45)
    state.diskSpace shouldBe None

    //when
    val result = reduce(state, action)

    //then
    assertFileListState(result, state.copy(
      diskSpace = Some(123.45)
    ))
  }
}
