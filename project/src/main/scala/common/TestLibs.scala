package common

import common.Libs._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._
import scommons.sbtplugin.project.CommonTestLibs

object TestLibs extends CommonTestLibs {

  override val scalaTestVersion = "3.2.14"
  override val scalaMockVersion = "5.2.0"

  lazy val scalaJsJavaSecureRandom = Def.setting("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0")

  lazy val scommonsNodejsTest = Def.setting("org.scommons.nodejs" %%% "scommons-nodejs-test" % scommonsNodejsVersion)
  lazy val scommonsReactTest = Def.setting("org.scommons.react" %%% "scommons-react-test" % scommonsReactVersion)
}
