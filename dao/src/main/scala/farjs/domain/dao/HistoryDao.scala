package farjs.domain.dao

import farjs.domain._

import scala.concurrent.Future

abstract class HistoryDao(val ctx: FarjsDBContext,
                          tableName: String,
                          maxItemsCount: Int) {

  import ctx._

  def getAll: Future[Seq[HistoryEntity]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT item, updated_at FROM $tableName ORDER BY updated_at",
      extractor = HistoryEntity.tupled
    ))
  }

  def save(entity: HistoryEntity): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (item, updated_at) VALUES (?, ?)" +
          "ON CONFLICT (item) DO UPDATE SET updated_at = excluded.updated_at",
        args = (entity.item, entity.updatedAt)
      )
      _ <- ctx.runAction(
        sql = s"DELETE FROM $tableName WHERE updated_at < " +
          "(SELECT min(updated_at) FROM (" +
          s"SELECT updated_at FROM $tableName ORDER BY updated_at DESC LIMIT ?" +
          "))",
        args = maxItemsCount
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction(s"DELETE FROM $tableName"))
  }
}
