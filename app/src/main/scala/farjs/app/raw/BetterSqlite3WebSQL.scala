package farjs.app.raw

import scommons.websql.raw.WebSQLDatabase

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** @see https://www.w3.org/TR/webdatabase/
  * @see https://github.com/nolanlawson/node-websql/
  */
object BetterSqlite3WebSQL {

  @js.native
  @JSImport("@farjs/better-sqlite3-websql", JSImport.Default)
  private object _openDatabase extends js.Function

  def openDatabase(name: String): WebSQLDatabase = {
    _openDatabase
      .asInstanceOf[js.Function4[String, String, String, Int, WebSQLDatabase]]
      .apply(name, "1.0", "description", 1)
  }
}
