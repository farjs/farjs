package farjs.domain

import scommons.websql.migrations.WebSqlMigrationBundle

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("./farjs/domain/bundle.json", JSImport.Namespace)
object FarjsDBMigrations extends WebSqlMigrationBundle
