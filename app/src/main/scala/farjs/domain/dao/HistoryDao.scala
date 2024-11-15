package farjs.domain.dao

import farjs.domain._
import farjs.filelist.history.History
import scommons.websql.io.dao.CommonDao

import scala.concurrent.Future
import scala.scalajs.js

class HistoryDao(val ctx: FarjsDBContext, kind: HistoryKindEntity, maxItemsCount: Int)
  extends CommonDao {

  import ctx._

  private val tableName = "history"
  
  private val rowExtractor: ((String, Option[String])) => History = {
    case (item, params) =>
      History(item, params.map(fromJson) match {
        case Some(p) => p: js.UndefOr[js.Object]
        case None => js.undefined: js.UndefOr[js.Object]
      })
  }

  private def fromJson(params: String): js.Object =
    js.JSON.parse(params).asInstanceOf[js.Object]

  private def toJson(params: js.Object): String =
    js.JSON.stringify(params)

  def getAll: Future[Seq[History]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT item, params FROM $tableName WHERE kind_id = ? ORDER BY updated_at",
      args = kind.id,
      extractor = rowExtractor
    ))
  }

  def getByItem(item: String): Future[Option[History]] = {
    getOne("getByItem", ctx.performIO(ctx.runQuery(
      sql = s"SELECT item, params FROM $tableName WHERE kind_id = ? AND item = ?",
      args = (kind.id, item),
      extractor = rowExtractor
    )))
  }

  def save(entity: History, updatedAt: Double): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (kind_id, item, params, updated_at) VALUES (?, ?, ?, ?)" +
          """ON CONFLICT (kind_id, item) DO UPDATE SET
            |  params = excluded.params,
            |  updated_at = excluded.updated_at
            |""".stripMargin,
        args = (kind.id, entity.item, entity.params.map(toJson).toOption, updatedAt)
      )
      _ <- ctx.runAction(
        sql = s"DELETE FROM $tableName WHERE kind_id = ? AND updated_at < " +
          "(SELECT min(updated_at) FROM (" +
          s"SELECT updated_at FROM $tableName WHERE kind_id = ? ORDER BY updated_at DESC LIMIT ?" +
          "))",
        args = (kind.id, kind.id, maxItemsCount)
      )
    } yield ()

    ctx.performIO(q)
  }
  
  def deleteAll(): Future[Long] = {
    ctx.performIO(ctx.runAction(s"DELETE FROM $tableName"))
  }
}
