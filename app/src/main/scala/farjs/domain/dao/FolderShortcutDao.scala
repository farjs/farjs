package farjs.domain.dao

import farjs.domain._

import scala.concurrent.Future

class FolderShortcutDao(val ctx: FarjsDBContext) {

  import ctx._
  
  private val tableName = "folder_shortcuts"

  def getAll: Future[Seq[FolderShortcut]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT id, path FROM $tableName ORDER BY id",
      extractor = FolderShortcut.tupled
    ))
  }

  def save(entity: FolderShortcut): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (id, path) VALUES (?, ?)" +
          "ON CONFLICT (id) DO UPDATE SET path = excluded.path",
        args = (entity.id, entity.path)
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def delete(id: Int): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"DELETE FROM $tableName WHERE id = ?",
        args = id
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction(s"DELETE FROM $tableName"))
  }
}
