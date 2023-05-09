package farjs.domain.dao

import farjs.domain._
import scommons.websql.io.dao.CommonDao

import scala.concurrent.Future

class FileViewHistoryDao(val ctx: FarjsDBContext, maxItemsCount: Int = 150)
  extends CommonDao {

  import ctx._

  private val tableName = "history_file_views"

  def getAll: Future[Seq[FileViewHistoryEntity]] = {
    ctx.performIO(ctx.runQuery(
      sql = s"SELECT path, is_edit, encoding, position, wrap, column, updated_at FROM $tableName ORDER BY updated_at",
      extractor = FileViewHistoryEntity.tupled
    ))
  }

  def getById(path: String, isEdit: Boolean): Future[Option[FileViewHistoryEntity]] = {
    getOne("getById", ctx.performIO(ctx.runQuery(
      sql = s"SELECT path, is_edit, encoding, position, wrap, column, updated_at FROM $tableName WHERE path = ? AND is_edit = ?",
      args = (path, isEdit),
      extractor = FileViewHistoryEntity.tupled
    )))
  }

  def save(entity: FileViewHistoryEntity): Future[Unit] = {
    val q = for {
      _ <- ctx.runAction(
        sql = s"INSERT INTO $tableName (path, is_edit, encoding, position, wrap, column, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)" +
          """ON CONFLICT (path, is_edit) DO UPDATE SET
            |  encoding = excluded.encoding,
            |  position = excluded.position,
            |  wrap = excluded.wrap,
            |  column = excluded.column,
            |  updated_at = excluded.updated_at
            |""".stripMargin,
        args = (entity.path, entity.isEdit, entity.encoding, entity.position, entity.wrap, entity.column, entity.updatedAt)
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
