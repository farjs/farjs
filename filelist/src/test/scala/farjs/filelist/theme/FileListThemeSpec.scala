package farjs.filelist.theme

import farjs.ui.theme.Theme
import scommons.react._

object FileListThemeSpec {

  def withThemeContext(element: ReactElement,
                       theme: FileListTheme = FileListTheme.defaultTheme): ReactElement = {

    <(Theme.Context.Provider)(^.contextValue := theme)(
      element
    )
  }
}
