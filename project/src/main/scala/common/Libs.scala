package common

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import scommons.sbtplugin.project.CommonLibs

object Libs extends CommonLibs {

  val scommonsReactVersion = "1.0.0-SNAPSHOT"

  lazy val scommonsReactCore = Def.setting("org.scommons.react" %%% "scommons-react-core" % scommonsReactVersion)
}
