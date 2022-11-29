package farjs.app

import farjs.app.raw.NodeGlobal
import scommons.websql.Database
import scommons.websql.migrations.{WebSqlMigrationBundle, WebSqlMigrations}

import scala.concurrent.Future

object FarjsDBMigrations {

  def apply(db: Database): Future[Unit] = {
    val module = "./farjs/domain/bundle.json"
    val bundle = NodeGlobal.require[WebSqlMigrationBundle](module)
    val migrations = new WebSqlMigrations(db)
    migrations.runBundle(bundle)
  }
}
