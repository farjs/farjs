package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.react.test.TestSpec

class FileListStateReducerSpec extends TestSpec {

  private val reduce = FileListStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FileListState()
  }
  
  it should "set params when FileListParamsChangedAction" in {
    //given
    val offset = 1
    val index = 2
    val selectedNames = Set("test")
    
    //when & then
    reduce(Some(FileListState()), FileListParamsChangedAction(offset, index, selectedNames)) shouldBe {
      FileListState(
        offset = offset,
        index = index,
        selectedNames = selectedNames
      )
    }
  }
  
  it should "set sorted items when FileListDirChangedAction(root)" in {
    //given
    val currDir = FileListDir("/", isRoot = true)
    val files = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    )
    
    //when & then
    reduce(Some(FileListState()), FileListDirChangedAction(None, currDir, files)) shouldBe {
      FileListState(
        currDir = currDir,
        items = List(
          FileListItem("dir 1", isDir = true),
          FileListItem("dir 2", isDir = true),
          FileListItem("file 1"),
          FileListItem("file 2")
        ),
        selectedNames = Set.empty
      )
    }
  }
  
  it should "add .. to items and set index when FileListDirChangedAction(not root)" in {
    //given
    val currDir = FileListDir("/root/sub-dir", isRoot = false)
    val files = List(
      FileListItem("file 2"),
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true),
      FileListItem("dir 1", isDir = true)
    )
    
    //when & then
    reduce(Some(FileListState(
      currDir = FileListDir("/root/sub-dir/dir 2", isRoot = false)
    )), FileListDirChangedAction(Some(".."), currDir, files)) shouldBe {
      FileListState(
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
      )
    }
  }
}
