package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsApp extends ScalaJsModule {

  override val id = "farjs-app"

  override val base: File = file("app")

  private val copyToTargetDir: TaskKey[Unit] =
    taskKey[Unit]("Copies npm package resources from root dir to target dir")

  override def definition: Project = super.definition
    .settings(
      publish / skip := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      scommonsResourcesFileFilter := scommonsResourcesFileFilter.value || "*.mjs",
      scommonsBundlesFileFilter := "*.sql",

      coverageExcludedPackages :=
        "farjs.app.FarjsApp" +
          ";farjs.app.raw",

      //TODO: temporarily disabled
      //  @see: https://github.com/scalameta/metabrowse/issues/271
//      fullOptJS / scalaJSLinkerConfig ~= {
//        _.withOptimizer(false)
//      },

      scalaJSUseMainModuleInitializer := false,

      Compile / copyToTargetDir := {
        def copyToDir(targetDir: File)(file: File): File = {
          val copy = targetDir / file.name
          IO.copyFile(file, copy)
          copy
        }
      
        val targetDir = (Compile / npmUpdate / crossTarget).value
        copyToDir(targetDir)(baseDirectory.value / ".." / "package.json")
        copyToDir(targetDir)(baseDirectory.value / ".." / "LICENSE.txt")
        copyToDir(targetDir)(baseDirectory.value / ".." / "README.md")
        targetDir
      },

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test",
    FarjsFs.definition,
    FarjsArchiver.definition,
    FarjsViewer.definition,
    FarjsCopyMove.definition,
    FarjsDao.definition
  )
}
