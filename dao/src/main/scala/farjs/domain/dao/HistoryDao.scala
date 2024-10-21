package farjs.domain.dao

import farjs.domain._
import scommons.websql.io.dao.CommonDao

import scala.concurrent.Future
import scala.scalajs.js

class HistoryDao(val ctx: FarjsDBContext, maxItemsCount: Int = 150)
  extends CommonDao {

  import ctx._

  private val tableName = "history"
  
  private val rowExtractor: ((Int, String, Option[String], Long)) => HistoryEntity = {
    case (kindId, item, params, updatedAt) =>
      HistoryEntity(kindId, item, params.map(fromJson), updatedAt)
  }

  private def fromJson(params: String): js.Object =
    js.JSON.parse(params).asInstanceOf[js.Object]

  private def toJson(params: js.Object): String =
    js.JSON.stringify(params)

  def getAll: Future[Seq[HistoryEntity]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT kind_id, item, params, updated_at FROM $tableName ORDER BY kind_id, updated_at",
      extractor = rowExtractor
    ))
  }

  def getByItem(kindId: Int, item: String): Future[Option[HistoryEntity]] = {
    getOne("getByItem", ctx.performIO(ctx.runQuery(
      sql = s"SELECT kind_id, item, params, updated_at FROM $tableName WHERE kind_id = ? AND item = ?",
      args = (kindId, item),
      extractor = rowExtractor
    )))
  }

  def save(entity: HistoryEntity): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (kind_id, item, params, updated_at) VALUES (?, ?, ?, ?)" +
          """ON CONFLICT (kind_id, item) DO UPDATE SET
            |  params = excluded.params,
            |  updated_at = excluded.updated_at
            |""".stripMargin,
        args = (entity.kindId, entity.item, entity.params.map(toJson), entity.updatedAt)
      )
      _ <- ctx.runAction(
        sql = s"DELETE FROM $tableName WHERE kind_id = ? AND updated_at < " +
          "(SELECT min(updated_at) FROM (" +
          s"SELECT updated_at FROM $tableName WHERE kind_id = ? ORDER BY updated_at DESC LIMIT ?" +
          "))",
        args = (entity.kindId, entity.kindId, maxItemsCount)
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction(s"DELETE FROM $tableName"))
  }
}
