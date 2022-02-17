package farjs.filelist

import scommons.nodejs.test.TestSpec

class FileListsStateSpec extends TestSpec {

  it should "return default state when state is None for FileListsStateReducer" in {
    //when & then
    FileListsStateReducer(None, "") shouldBe FileListsState()
  }
}
