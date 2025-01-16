package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsApp extends ScalaJsModule {

  override val id = "farjs-app"

  override val base: File = file("app")

  override def definition: Project = super.definition
    .settings(
      publish / skip := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      scommonsResourcesFileFilter := scommonsResourcesFileFilter.value || "*.mjs",

      coverageExcludedPackages :=
        "farjs.app.FarjsApp" +
          ";farjs.app.raw" +
          //to avoid Scala.js error: Found a dangling UndefinedParam
          ";farjs.domain.dao.HistoryDao",

      //TODO: temporarily disabled
      //  @see: https://github.com/scalameta/metabrowse/issues/271
//      fullOptJS / scalaJSLinkerConfig ~= {
//        _.withOptimizer(false)
//      },

      scalaJSUseMainModuleInitializer := false,

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test",
    FarjsFile.definition % "compile->compile;test->test",
    FarjsFs.definition,
    FarjsArchiver.definition,
    FarjsViewer.definition,
    FarjsCopyMove.definition
  )
}
