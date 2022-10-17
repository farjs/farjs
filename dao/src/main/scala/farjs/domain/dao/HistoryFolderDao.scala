package farjs.domain.dao

import farjs.domain._

import scala.concurrent.Future

class HistoryFolderDao(val ctx: FarjsDBContext) {

  import ctx._

  def getAll: Future[Seq[HistoryFolder]] = {
    ctx.performIO(ctx.runQuery(
      sql = "SELECT path, updated_at FROM history_folders ORDER BY updated_at",
      extractor = HistoryFolder.tupled
    ))
  }

  def save(entity: HistoryFolder, keepLast: Int): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = "INSERT INTO history_folders (path, updated_at) VALUES (?, ?)" +
          "ON CONFLICT (path) DO UPDATE SET updated_at = excluded.updated_at",
        args = (entity.path, entity.updatedAt)
      )
      _ <- ctx.runAction(
        sql = "DELETE FROM history_folders WHERE updated_at < " +
          "(SELECT min(updated_at) FROM (" +
          "SELECT updated_at FROM history_folders ORDER BY updated_at DESC LIMIT ?" +
          "))",
        args = keepLast
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction("DELETE FROM history_folders"))
  }
}
