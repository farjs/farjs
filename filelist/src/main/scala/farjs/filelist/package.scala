package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object filelist {

  @js.native
  @JSImport("@farjs/filelist/FileListStateReducer.mjs", JSImport.Default)
  object FileListStateReducer extends js.Function2[FileListState, js.Any, FileListState] {

    def apply(state: FileListState, action: js.Any): FileListState = js.native
  }

  @js.native
  @JSImport("@farjs/filelist/FileListColumn.mjs", JSImport.Default)
  object FileListColumn extends ReactClass

  @js.native
  @JSImport("@farjs/filelist/FileListView.mjs", JSImport.Default)
  object FileListView extends ReactClass

  @js.native
  @JSImport("@farjs/filelist/FileList.mjs", JSImport.Default)
  object FileList extends ReactClass

  @js.native
  @JSImport("@farjs/filelist/FileListPanelView.mjs", JSImport.Default)
  object FileListPanelView extends ReactClass

  @js.native
  @JSImport("@farjs/filelist/FileListQuickSearch.mjs", JSImport.Default)
  object FileListQuickSearch extends ReactClass

  @js.native
  @JSImport("@farjs/filelist/FileListPanel.mjs", JSImport.Default)
  object FileListPanel extends ReactClass

  @js.native
  @JSImport("../filelist/FileListUiPlugin.mjs", JSImport.Default)
  object FileListUiPlugin extends FileListPlugin(js.native)
}
