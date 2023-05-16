package definitions

import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsFile extends ScalaJsModule {

  override val id = "farjs-file"

  override val base: File = file("file")

  override def definition: Project = {
    super.definition
      .settings(
        description := "File encodings and other file utils",

        coverageExcludedPackages := "farjs.file.raw",

        Compile / npmDependencies ++= Seq(
          "iconv-lite" -> "0.6.3"
        )
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition
  )
}
