package farjs.ui.theme

import scommons.react._

object ThemeSpec {

  def withThemeContext(element: ReactElement, theme: Theme = DefaultTheme): ReactElement = {
    <(Theme.Context.Provider)(^.contextValue := theme)(
      element
    )
  }
}
