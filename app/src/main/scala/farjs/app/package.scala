package farjs

import farjs.app.raw.BetterSqlite3Database
import farjs.filelist.FileListActions

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object app {

  @js.native
  @JSImport("../app/FarjsDBMigrations.mjs", JSImport.Default)
  object FarjsDBMigrations extends js.Function1[String, js.Promise[BetterSqlite3Database]] {

    def apply(dbName: String): js.Promise[BetterSqlite3Database] = js.native

    def prepareDB(actions: FileListActions, appData: FarjsData): js.Promise[BetterSqlite3Database] = js.native
  }
}
