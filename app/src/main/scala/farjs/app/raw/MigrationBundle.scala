package farjs.app.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
trait MigrationBundleItem extends js.Object {

  def file: String = js.native
  def content: String = js.native
}

@js.native
trait MigrationBundle extends js.Array[MigrationBundleItem]

@js.native
@JSImport("@farjs/better-sqlite3-migrate", JSImport.Namespace)
object MigrationBundle extends js.Object {

  def runBundle(db: BetterSqlite3Database, bundle: MigrationBundle): js.Promise[Unit] = js.native  
}
