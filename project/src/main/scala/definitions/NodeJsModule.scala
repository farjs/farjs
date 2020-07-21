package definitions

import common.{Libs, TestLibs}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait NodeJsModule extends FarjsModule {

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(NodeJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-nodejs", "scommons-nodejs-core", None),

    ("scommons-nodejs", "scommons-nodejs-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    Libs.scommonsNodejsCore.value
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    TestLibs.scommonsNodejsTest.value
  ).map(_ % "test"))
}

object NodeJsModule {

  val settings: Seq[Setting[_]] = Seq(
    scalaJSModuleKind := ModuleKind.CommonJSModule,

    //Opt-in @ScalaJSDefined by default
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    requireJsDomEnv in Test := false,
    version in webpack := "3.5.5",
    emitSourceMaps := false,

    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "build",
        base / "node_modules"
      )
    }
  )
}
