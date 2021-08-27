package common

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import scommons.sbtplugin.project.CommonLibs

object Libs extends CommonLibs {

  val scommonsNodejsVersion = "0.5.0"
  val scommonsReactVersion = "1.0.0-SNAPSHOT"

  lazy val scommonsNodejsCore = Def.setting("org.scommons.nodejs" %%% "scommons-nodejs-core" % scommonsNodejsVersion)
  lazy val scommonsReactCore = Def.setting("org.scommons.react" %%% "scommons-react-core" % scommonsReactVersion)
  lazy val scommonsReactRedux = Def.setting("org.scommons.react" %%% "scommons-react-redux" % scommonsReactVersion)
}
