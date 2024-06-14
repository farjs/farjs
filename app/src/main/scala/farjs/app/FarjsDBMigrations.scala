package farjs.app

import farjs.app.raw.{BetterSqlite3Database, MigrationBundle}
import scommons.nodejs.raw.{FS, FileOptions, URL}
import scommons.websql.raw.WebSQLDatabase

import scala.concurrent.Future
import scala.scalajs.js

object FarjsDBMigrations {

  def apply(db: WebSQLDatabase): Future[Unit] = {
    val module = "./migrations/bundle.json"
    val url = new URL(module, js.`import`.meta.url.asInstanceOf[String])
    val json = FS.readFileSync(url, new FileOptions {
      override val encoding = "utf8"
    })
    val bundle = js.JSON.parse(
      text = json,
      reviver = js.undefined.asInstanceOf[js.Function2[js.Any, js.Any, js.Any]]
    ).asInstanceOf[MigrationBundle]

    val rawDb = db._db.asInstanceOf[js.Dynamic]._db
    MigrationBundle.runBundle(rawDb.asInstanceOf[BetterSqlite3Database], bundle).toFuture
  }
}
