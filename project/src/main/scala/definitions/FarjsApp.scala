package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.BundlingMode
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarjsApp extends ScalaJsModule {

  override val id = "farjs-app"

  override val base: File = file("app")

  override def definition: Project = super.definition
    .settings(
      skip in publish := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      coverageExcludedPackages := {
        "farjs.app.FarjsApp" +
          ";farjs.app.FarjsActions"
      },

      scalaJSUseMainModuleInitializer := false,
      webpackBundlingMode := BundlingMode.LibraryOnly(),
      version in webpack := "4.29.0",

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),
      
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

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition
  )
}
