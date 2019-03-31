package definitions

import common.{Libs, TestLibs}
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.BundlingMode
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarcApp extends ScalaJsModule {

  override val id = "scommons-farc-app"

  override val base: File = file("app")

  override def definition: Project = super.definition
    .settings(
      skip in publish := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      coverageExcludedPackages := {
        "scommons.react.blessed.raw" +
          ";scommons.nodejs.raw"
      },

      scalaJSUseMainModuleInitializer := true,
      webpackBundlingMode := BundlingMode.LibraryOnly(),

      useYarn := true,
      
      npmDependencies in Compile ++= Seq(
        "blessed" -> "0.1.81",
        //"neo-blessed" -> "0.2.0",
        //"@medv/blessed" -> "2.0.0",
        "react-blessed" -> "0.5.0",
        "react-reconciler" -> "~0.20.3"
      ),

      npmDevDependencies in Compile ++= Seq(
        "webpack-merge" -> "4.1.0",
        "webpack-node-externals" -> "1.7.2"
      ),

      //dev
      webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js"),
      //prod
      webpackConfigFile in fullOptJS := Some(baseDirectory.value / "dev.webpack.config.js")
      //reload workflow and tests
      //webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js")
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override val superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-react", "scommons-react-core", None),

    ("scommons-react", "scommons-react-test", Some("test"))
  )

  override val runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    Libs.scommonsReactCore.value
  ))

  override val testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    TestLibs.scommonsReactTest.value
  ).map(_ % "test"))
}
