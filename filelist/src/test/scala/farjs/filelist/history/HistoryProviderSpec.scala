package farjs.filelist.history

import scommons.react._

object HistoryProviderSpec {

  def withHistoryProvider(element: ReactElement,
                          historyProvider: HistoryProvider = new MockHistoryProvider
                         ): ReactElement = {

    <(HistoryProvider.Context.Provider)(^.contextValue := historyProvider)(
      element
    )
  }
}
