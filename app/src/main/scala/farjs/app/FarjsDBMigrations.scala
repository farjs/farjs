package farjs.app

import scommons.nodejs.raw.{FS, FileOptions, URL}
import scommons.websql.Database
import scommons.websql.migrations.{WebSqlMigrationBundle, WebSqlMigrations}

import scala.concurrent.Future
import scala.scalajs.js

object FarjsDBMigrations {

  def apply(db: Database): Future[Unit] = {
    val module = "./migrations/bundle.json"
    val url = new URL(module, js.`import`.meta.url.asInstanceOf[String])
    val json = FS.readFileSync(url, new FileOptions {
      override val encoding = "utf8"
    })
    val bundle = js.JSON.parse(
      text = json,
      reviver = js.undefined.asInstanceOf[js.Function2[js.Any, js.Any, js.Any]]
    ).asInstanceOf[WebSqlMigrationBundle]

    val migrations = new WebSqlMigrations(db)
    migrations.runBundle(bundle)
  }
}
