package definitions

import common.{Libs, TestLibs}
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonNodeJsModule

trait ScalaJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-react", "scommons-react-core", None),
      ("scommons-react", "scommons-react-redux", None),

      ("scommons-react", "scommons-react-test", Some("test"))
    )
  }

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsReactCore.value,
      Libs.scommonsReactRedux.value
    )
  }

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.testDependencies.value ++ Seq(
      TestLibs.scommonsReactTest.value
    ).map(_ % "test")
  }
}

object ScalaJsModule {

  val settings: Seq[Setting[_]] = Seq(
    webpack / version := "5.74.0"
  )
}
