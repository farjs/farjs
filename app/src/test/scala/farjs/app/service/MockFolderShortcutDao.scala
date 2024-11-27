package farjs.app.service

import farjs.domain.FolderShortcut
import farjs.domain.dao.FolderShortcutDao

import scala.scalajs.js

object MockFolderShortcutDao {

  //noinspection NotImplementedCode
  def apply(
             getAllMock: () => js.Promise[js.Array[FolderShortcut]] = () => ???,
             saveMock: (FolderShortcut) => js.Promise[Unit] = _ => ???,
             deleteMock: Int => js.Promise[Unit] = _ => ???,
             deleteAllMock: () => js.Promise[Unit] = () => ???
           ): FolderShortcutDao = {
    
    new FolderShortcutDao {

      override def getAll(): js.Promise[js.Array[FolderShortcut]] = getAllMock()

      override def save(entity: FolderShortcut): js.Promise[Unit] = saveMock(entity)

      override def delete(id: Int): js.Promise[Unit] = deleteMock(id)

      override def deleteAll(): js.Promise[Unit] = deleteAllMock()
    }
  }
}
