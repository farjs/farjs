package farjs.app.service

import farjs.app.service.HistoryProviderImpl.limitMaxItemsCount
import farjs.domain.{FarjsDBContext, HistoryKindEntity}
import farjs.domain.dao.{HistoryDao, HistoryKindDao}
import farjs.filelist.history._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.util.control.NonFatal

class HistoryProviderImpl(kindDao: HistoryKindDao, ctx: FarjsDBContext) extends HistoryProvider {

  private var services = Map.empty[String, HistoryService]
  private val noopService = new NoopHistoryService

  override def get(kind: HistoryKind): js.Promise[HistoryService] = {
    services.get(kind.name) match {
      case Some(service) => js.Promise.resolve[HistoryService](service)
      case None =>
        kindDao.upsert(HistoryKindEntity(-1, kind.name)).toFuture.map { kindEntity =>
          val service = new HistoryServiceImpl(
            new HistoryDao(ctx, kindEntity, limitMaxItemsCount(kind.maxItemsCount))
          )
          services = services.updated(kind.name, service)
          service
        }.recover {
          case NonFatal(ex) =>
            Console.err.println(s"Failed to upsert history kind '${kind.name}', error: $ex")
            noopService
        }.toJSPromise
    }
  }
}

object HistoryProviderImpl {

  private[service] def limitMaxItemsCount(maxCount: Int): Int =
    Math.min(Math.max(maxCount, 5), 150)
}

class NoopHistoryService extends HistoryService {

  override def getAll: js.Promise[js.Array[History]] =
    js.Promise.resolve[js.Array[History]](js.Array[History]())

  override def getOne(item: String): js.Promise[js.UndefOr[History]] =
    js.Promise.resolve[js.UndefOr[History]](js.undefined)

  override def save(h: History): js.Promise[Unit] =
    js.Promise.resolve[Unit](())
}
