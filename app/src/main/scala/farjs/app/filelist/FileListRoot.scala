package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.app.filelist.fs.FSServices
import farjs.filelist.FileListServices
import scommons.react._

class FileListRoot(module: FileListModule) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <(FileListServices.Context.Provider)(^.contextValue := module.fileListServices)(
      <(FSServices.Context.Provider)(^.contextValue := module.fsServices)(
        <(fileListComp).empty
      )
    )
  }
}

object FileListRoot {
  
  private[filelist] var fileListComp: ReactClass = FileListBrowserController()
}
