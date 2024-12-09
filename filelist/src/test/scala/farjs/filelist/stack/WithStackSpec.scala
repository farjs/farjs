package farjs.filelist.stack

import scommons.react._
import scommons.react.blessed.BlessedElement

object WithStackSpec {

  def withContext(element: ReactElement,
                  isRight: Boolean = false,
                  panelInput: BlessedElement = null,
                  stack: PanelStack = null,
                  width: Int = 0,
                  height: Int = 0
                 ): ReactElement = {

    <(WithStack.Context.Provider)(^.contextValue := WithStackProps(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    ))(
      element
    )
  }
}
