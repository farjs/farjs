package definitions

import common.{Libs, TestLibs}
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonNodeJsModule
import scoverage.ScoverageKeys.{coverageEnabled, coverageScalacPluginVersion}

trait ScalaJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    Nil
//    super.superRepoProjectsDependencies ++ Seq(
//      ("scommons-react", "scommons-react-core", None),
//      ("scommons-react", "scommons-react-redux", None),
//
//      ("scommons-react", "scommons-react-test", Some("test"))
//    )
  }

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsReactCore.value,
      Libs.scommonsReactRedux.value
    )
  }

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.testDependencies.value ++ Seq(
      TestLibs.scommonsReactTest.value,
      TestLibs.scalaJsJavaSecureRandom.value
    ).map(_ % "test")
  }
}

object ScalaJsModule {

  val settings: Seq[Setting[_]] = Seq(
    webpack / version := "5.74.0",

    scalaVersion := "2.13.8",
    scalacOptions ++= Seq(
      //see:
      //  http://www.scala-js.org/news/2021/12/10/announcing-scalajs-1.8.0/
      "-P:scalajs:nowarnGlobalExecutionContext"
    ),

    //NOTE:
    // we explicitly set scoverage runtime/plugin version that supports scalaVersion
    // instead of upgrading sbt-scoverage plugin since its newer versions (1.8+) are 10x slower!!!
    //
    coverageScalacPluginVersion := "1.4.11",
    libraryDependencies ~= { modules =>
      modules.filter(_.organization != "org.scoverage")
    },
    libraryDependencies ++= {
      if (coverageEnabled.value) {
        val scalaVer = scalaVersion.value
        Seq(
          "org.scoverage" %% "scalac-scoverage-runtime_sjs1" % coverageScalacPluginVersion.value,
          "org.scoverage" % s"scalac-scoverage-plugin_$scalaVer" % coverageScalacPluginVersion.value % "scoveragePlugin"
        )
      }
      else Nil
    }
  )
}
