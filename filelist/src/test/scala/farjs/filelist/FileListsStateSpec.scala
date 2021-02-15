package farjs.filelist

import scommons.nodejs.test.TestSpec

class FileListsStateSpec extends TestSpec {

  it should "return active list state when activeList" in {
    //given
    val left = FileListState(isActive = true)
    val right = FileListState(isRight = true)
    
    //when & then
    FileListsState(left, right).activeList shouldBe left
    FileListsState(left.copy(isActive = false), right).activeList shouldBe right
  }
}
