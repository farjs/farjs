package farjs.filelist.api

import scala.scalajs.js

object MockFileListApi {

  //noinspection NotImplementedCode
  def apply(
             capabilitiesMock: js.Set[FileListCapability] = js.Set.empty,
             readDir2Mock: (js.UndefOr[String], String) => js.Promise[FileListDir] = (_, _) => ???,
             readDirMock: String => js.Promise[FileListDir] = _ => ???,
             deleteMock: (String, js.Array[FileListItem]) => js.Promise[Unit] = (_, _) => ???,
             mkDirsMock: js.Array[String] => js.Promise[Unit] = _ => ???,
             readFileMock: (js.Array[String], FileListItem, Double) => js.Promise[FileSource] = (_, _, _) => ???,
             writeFileMock: (js.Array[String], String, FileListItem => js.Promise[js.UndefOr[Boolean]]) => js.Promise[js.UndefOr[FileTarget]] = (_, _, _) => ???
           ): FileListApi = {

    new FileListApi {

      override val capabilities: js.Set[FileListCapability] = capabilitiesMock

      override def readDir(parent: js.UndefOr[String], dir: String): js.Promise[FileListDir] =
        readDir2Mock(parent, dir)

      override def readDir(targetDir: String): js.Promise[FileListDir] =
        readDirMock(targetDir)

      override def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] =
        deleteMock(parent, items)

      override def mkDirs(dirs: js.Array[String]): js.Promise[Unit] =
        mkDirsMock(dirs)

      override def readFile(parentDirs: js.Array[String],
                            file: FileListItem,
                            position: Double): js.Promise[FileSource] = {
        
        readFileMock(parentDirs, file, position)
      }

      override def writeFile(parentDirs: js.Array[String],
                             fileName: String,
                             onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]
                            ): js.Promise[js.UndefOr[FileTarget]] = {

        writeFileMock(parentDirs, fileName, onExists)
      }
    }
  }
}
