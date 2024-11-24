package farjs.domain.dao

import farjs.domain._
import scommons.websql.io.dao.CommonDao

import scala.concurrent.Future

class HistoryKindDao(val ctx: FarjsDBContext) extends CommonDao {

  import ctx._

  private val tableName = "history_kinds"

  private val rowExtractor: ((Int, String)) => HistoryKindEntity = {
    case (id, name) => HistoryKindEntity(id, name)
  }

  def getAll: Future[Seq[HistoryKindEntity]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT id, name FROM $tableName ORDER BY id",
      extractor = rowExtractor
    ))
  }

  def upsert(entity: HistoryKindEntity): Future[HistoryKindEntity] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (name) VALUES (?) ON CONFLICT (name) DO NOTHING",
        args = entity.name
      )
      res <- ctx.runQuery(
        sql = s"SELECT id, name FROM $tableName WHERE name = ?",
        args = entity.name,
        extractor = rowExtractor
      )
    } yield res.head

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction(s"DELETE FROM $tableName"))
  }
}