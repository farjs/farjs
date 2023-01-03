package farjs.app.filelist.service

import farjs.domain.FolderShortcut
import farjs.domain.dao.FolderShortcutDao
import farjs.fs.popups.FolderShortcutsService

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

class FolderShortcutsServiceImpl(dao: FolderShortcutDao)
  extends FolderShortcutsService {

  def getAll: Future[Seq[Option[String]]] = {
    dao.getAll.map { shortcuts =>
      val res = ArrayBuffer.fill(10)(Option.empty[String])
      shortcuts.take(10).foreach { shortcut =>
        res.update(shortcut.id, Some(shortcut.path))
      }
      res.toList
    }.recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to read folder shortcuts, error: $ex")
        Nil
    }
  }

  def save(index: Int, path: String): Future[Unit] = {
    val entity = FolderShortcut(index, path)
    dao.save(entity).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to save folder shortcut, error: $ex")
    }
  }

  def delete(index: Int): Future[Unit] = {
    dao.delete(index).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to delete folder shortcut, error: $ex")
    }
  }
}
