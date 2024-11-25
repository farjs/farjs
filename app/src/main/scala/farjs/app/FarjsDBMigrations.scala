package farjs.app

import farjs.app.raw.{BetterSqlite3Database, MigrationBundle}
import scommons.nodejs.raw.URL

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object FarjsDBMigrations {

  def apply(db: BetterSqlite3Database): Future[Unit] = {
    val module = "../dao/migrations/bundle.json"
    val url = new URL(module, js.`import`.meta.url.asInstanceOf[String])

    for {
      bundle <- MigrationBundle.readBundle(url).toFuture
      _ <- MigrationBundle.runBundle(db, bundle).toFuture
    } yield ()
  }
}
