package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.filelist.FileListServices
import scommons.react._

class FileListRoot(services: FileListServices) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <(FileListServices.Context.Provider)(^.contextValue := services)(
      <(fileListComp).empty
    )
  }
}

object FileListRoot {
  
  private[filelist] var fileListComp: ReactClass = FileListBrowserController()
}
