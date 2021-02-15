package farjs.filelist

import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.nodejs.test.TestSpec

class FileListStateSpec extends TestSpec {

  it should "return current item depending on state when currentItem" in {
    //given
    val item1 = FileListItem("dir 1")
    val item2 = FileListItem("file 1")
    val currDir = FileListDir("/folder", isRoot = false, List(item1, item2))
    
    //when & then
    FileListState().currentItem shouldBe None
    FileListState(index = 1).currentItem shouldBe None
    FileListState(offset = 1).currentItem shouldBe None
    FileListState(offset = 1).currentItem shouldBe None
    FileListState(currDir = currDir).currentItem shouldBe Some(item1)
    FileListState(index = 1, currDir = currDir).currentItem shouldBe Some(item2)
    FileListState(offset = 1, currDir = currDir).currentItem shouldBe Some(item2)
    FileListState(offset = 1, index = 1, currDir = currDir).currentItem shouldBe None
  }
  
  it should "return selected items depending on state when selectedItems" in {
    //given
    val item1 = FileListItem("dir 1")
    val item2 = FileListItem("file 1")
    val currDir = FileListDir("/folder", isRoot = false, List(item1, item2))
    
    //when & then
    FileListState().selectedItems shouldBe Nil
    FileListState(currDir = currDir).selectedItems shouldBe Nil
    FileListState(currDir = currDir, selectedNames = Set("dir 1")).selectedItems shouldBe List(item1)
    FileListState(currDir = currDir, selectedNames = Set("file 1")).selectedItems shouldBe List(item2)
    FileListState(currDir = currDir, selectedNames = Set("file 123")).selectedItems shouldBe Nil
  }
}
