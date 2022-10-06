package common

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import scommons.sbtplugin.project.CommonLibs

object Libs extends CommonLibs {

  val scommonsNodejsVersion = "0.9.0"
  val scommonsReactVersion = "0.9.0"
  val scommonsWebSqlVersion = "0.9.0"

  lazy val scommonsNodejsCore = Def.setting("org.scommons.nodejs" %%% "scommons-nodejs-core" % scommonsNodejsVersion)
  lazy val scommonsReactCore = Def.setting("org.scommons.react" %%% "scommons-react-core" % scommonsReactVersion)
  lazy val scommonsReactRedux = Def.setting("org.scommons.react" %%% "scommons-react-redux" % scommonsReactVersion)

  lazy val scommonsWebSqlMigrations = Def.setting("org.scommons.websql" %%% "scommons-websql-migrations" % scommonsWebSqlVersion)
  lazy val scommonsWebSqlIO = Def.setting("org.scommons.websql" %%% "scommons-websql-io" % scommonsWebSqlVersion)
}
