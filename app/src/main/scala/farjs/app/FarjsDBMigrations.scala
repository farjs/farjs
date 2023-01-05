package farjs.app

import farjs.app.raw.NodeGlobal
import scommons.websql.Database
import scommons.websql.migrations.{WebSqlMigrationBundle, WebSqlMigrations}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object FarjsDBMigrations {

  def apply(db: Database): Future[Unit] = {
    val module = "farjs/domain/bundle.json"
    val bundle = NodeGlobal.require[WebSqlMigrationBundle](s"./$module")
    clearCache(module)

    val migrations = new WebSqlMigrations(db)
    migrations.runBundle(bundle)
  }
  
  private def clearCache(moduleSuffix: String): Unit = {
    val cache = global.require.cache
    val cacheDict = cache.asInstanceOf[js.Dictionary[js.Any]]
    val maybeKey = cacheDict.keys.find(_.endsWith(moduleSuffix))
    maybeKey.foreach { key =>
      cacheDict -= key
    }
  }
}
