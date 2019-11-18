package farclone.ui.filelist.popups

import farclone.ui.filelist.popups.FileListPopupsActions._
import scommons.react.test.TestSpec

class FileListPopupsStateReducerSpec extends TestSpec {

  private val reduce = FileListPopupsStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FileListPopupsState()
  }
  
  it should "set showHelpPopup when FileListHelpAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListHelpAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showHelpPopup = true)
    }
  }
  
  it should "set showExitPopup when FileListExitAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListExitAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showExitPopup = true)
    }
  }
}
