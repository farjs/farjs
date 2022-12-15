package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopupsActions._
import scommons.react.test.TestSpec

class FSPopupsStateReducerSpec extends TestSpec {

  private val reduce = FSPopupsStateReducer.apply _
  
  it should "return default state when state is None" in {
    //when & then
    reduce(None, "") shouldBe FSPopupsState()
  }
  
  it should "set showFoldersHistoryPopup when FoldersHistoryPopupAction" in {
    //given
    val state = FSPopupsState()
    val action = FoldersHistoryPopupAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showFoldersHistoryPopup = true)
    }
  }

  it should "set showFolderShortcutsPopup when FolderShortcutsPopupAction" in {
    //given
    val state = FSPopupsState()
    val action = FolderShortcutsPopupAction(show = true)
    
    //when & then
    reduce(Some(state), action) shouldBe {
      state.copy(showFolderShortcutsPopup = true)
    }
  }
}