package farjs.fs

import farjs.fs.popups._
import scommons.react._

object FSServicesSpec {

  def withServicesContext(element: ReactElement,
                          folderShortcutsService: FolderShortcutsService = new MockFolderShortcutsService
                         ): ReactElement = {

    <(FSServices.Context.Provider)(^.contextValue := new FSServices {
      val folderShortcuts = folderShortcutsService
    })(
      element
    )
  }
}
