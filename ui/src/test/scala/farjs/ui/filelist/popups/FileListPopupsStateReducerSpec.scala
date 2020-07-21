package farjs.ui.filelist.popups

import farjs.ui.filelist.popups.FileListPopupsActions._
import scommons.react.test.TestSpec

class FileListPopupsStateReducerSpec extends TestSpec {

  private val reduce = FileListPopupsStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FileListPopupsState()
  }
  
  it should "set showHelpPopup when FileListPopupHelpAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListPopupHelpAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showHelpPopup = true)
    }
  }
  
  it should "set showExitPopup when FileListPopupExitAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListPopupExitAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showExitPopup = true)
    }
  }
  
  it should "set showDeletePopup when FileListPopupDeleteAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListPopupDeleteAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showDeletePopup = true)
    }
  }
  
  it should "set showMkFolderPopup when FileListPopupMkFolderAction" in {
    //given
    val state = FileListPopupsState()
    val action = FileListPopupMkFolderAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showMkFolderPopup = true)
    }
  }
}
