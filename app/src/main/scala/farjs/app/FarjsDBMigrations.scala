package farjs.app

import farjs.app.raw.{BetterSqlite3Database, MigrationBundle}
import scommons.nodejs.raw.URL
import scommons.websql.raw.WebSQLDatabase

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object FarjsDBMigrations {

  def apply(db: WebSQLDatabase): Future[Unit] = {
    val module = "./migrations/bundle.json"
    val url = new URL(module, js.`import`.meta.url.asInstanceOf[String])

    MigrationBundle.readBundle(url).toFuture.flatMap { bundle =>
      val rawDb = db._db.asInstanceOf[js.Dynamic]._db
      MigrationBundle.runBundle(rawDb.asInstanceOf[BetterSqlite3Database], bundle).toFuture
    }
  }
}
