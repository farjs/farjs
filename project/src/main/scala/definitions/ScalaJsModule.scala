package definitions

import common.{Libs, TestLibs}
import sbt._
import scommons.sbtplugin.project.CommonClientModule

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait ScalaJsModule extends NodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(CommonClientModule.settings: _*)
      .settings(
        requireJsDomEnv in Test := false
      )
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
