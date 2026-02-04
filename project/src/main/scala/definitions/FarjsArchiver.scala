package definitions

import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsArchiver extends ScalaJsModule {

  override val id = "farjs-archiver"

  override val base: File = file("archiver")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Multiple archivers filelist api implementation",

        coverageExcludedPackages :=
          "farjs.archiver" +
            ";farjs.archiver.zip"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition % "compile->compile;test->test",
    FarjsFileList.definition % "compile->compile;test->test"
  )
}
