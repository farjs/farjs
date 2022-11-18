package common

import common.Libs._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import scommons.sbtplugin.project.CommonTestLibs

object TestLibs extends CommonTestLibs {

  lazy val scommonsNodejsTest = Def.setting("org.scommons.nodejs" %%% "scommons-nodejs-test" % scommonsNodejsVersion)
  lazy val scommonsReactTest = Def.setting("org.scommons.react" %%% "scommons-react-test" % scommonsReactVersion)
}
