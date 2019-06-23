package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.react.test.TestSpec

class FileListsStateReducerSpec extends TestSpec {

  private val reduce = FileListsStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FileListsState()
  }
  
  it should "set params when FileListParamsChangedAction" in {
    //given
    val state = FileListsState()
    val offset = 1
    val index = 2
    val selectedNames = Set("test")
    
    //when & then
    reduce(Some(state), FileListParamsChangedAction(isRight = false, offset, index, selectedNames)) shouldBe {
      state.copy(left = FileListState(
        offset = offset,
        index = index,
        selectedNames = selectedNames
      ))
    }
    //when & then
    reduce(Some(state), FileListParamsChangedAction(isRight = true, offset, index, selectedNames)) shouldBe {
      state.copy(right = FileListState(
        offset = offset,
        index = index,
        selectedNames = selectedNames,
        isRight = true
      ))
    }
  }
  
  it should "set sorted items when FileListDirChangedAction(root)" in {
    //given
    val state = FileListsState()
    val currDir = FileListDir("/", isRoot = true)
    val files = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    )
    
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = false, None, currDir, files)) shouldBe {
      state.copy(left = FileListState(
        currDir = currDir,
        items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        ),
        selectedNames = Set.empty
      ))
    }
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = true, None, currDir, files)) shouldBe {
      state.copy(right = FileListState(
        currDir = currDir,
        items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        ),
        selectedNames = Set.empty,
        isRight = true
      ))
    }
  }
  
  it should "add .. to items and set index when FileListDirChangedAction(not root)" in {
    //given
    val stateDir = FileListDir("/root/sub-dir/dir 2", isRoot = false)
    val state = FileListsState(
      left = FileListState(currDir = stateDir),
      right = FileListState(currDir = stateDir, isRight = true)
    )
    val currDir = FileListDir("/root/sub-dir", isRoot = false)
    val files = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    )
    
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = false, Some(".."), currDir, files)) shouldBe {
      state.copy(left = FileListState(
        index = 2,
        currDir = currDir,
        items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        ),
        selectedNames = Set.empty
      ))
    }
    //when & then
    reduce(Some(state), FileListDirChangedAction(isRight = true, Some(".."), currDir, files)) shouldBe {
      state.copy(right = FileListState(
        index = 2,
        currDir = currDir,
        items = List(
          FileListItem.up,
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        ),
        selectedNames = Set.empty,
        isRight = true
      ))
    }
  }
}
