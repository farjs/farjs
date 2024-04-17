package farjs.filelist.api

import scala.scalajs.js

object MockFileListApi {

  //noinspection NotImplementedCode
  def apply(
             capabilitiesMock: js.Set[FileListCapability] = js.Set.empty,
             readDirMock: (String, js.UndefOr[String]) => js.Promise[FileListDir] = (_, _) => ???,
             deleteMock: (String, js.Array[FileListItem]) => js.Promise[Unit] = (_, _) => ???,
             mkDirsMock: js.Array[String] => js.Promise[String] = _ => ???,
             readFileMock: (String, FileListItem, Double) => js.Promise[FileSource] = (_, _, _) => ???,
             writeFileMock: (String, String, FileListItem => js.Promise[js.UndefOr[Boolean]]) => js.Promise[js.UndefOr[FileTarget]] = (_, _, _) => ???
           ): FileListApi = {

    new FileListApi {

      override val capabilities: js.Set[FileListCapability] = capabilitiesMock

      override def readDir(path: String, dir: js.UndefOr[String]): js.Promise[FileListDir] =
        readDirMock(path, dir)

      override def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] =
        deleteMock(parent, items)

      override def mkDirs(dirs: js.Array[String]): js.Promise[String] =
        mkDirsMock(dirs)

      override def readFile(parent: String,
                            file: FileListItem,
                            position: Double): js.Promise[FileSource] = {
        
        readFileMock(parent, file, position)
      }

      override def writeFile(parent: String,
                             fileName: String,
                             onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]
                            ): js.Promise[js.UndefOr[FileTarget]] = {

        writeFileMock(parent, fileName, onExists)
      }
    }
  }
}
