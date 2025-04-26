package definitions

import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsViewer extends ScalaJsModule {

  override val id = "farjs-viewer"

  override val base: File = file("viewer")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Internal file viewer implementation",

        coverageExcludedPackages :=
          //to avoid Scala.js error: Found a dangling UndefinedParam
          "farjs.viewer.ViewerFileReader;farjs.viewer.ViewerContent"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition % "compile->compile;test->test",
    FarjsFileList.definition % "compile->compile;test->test",
    FarjsFile.definition % "compile->compile;test->test"
  )
}
