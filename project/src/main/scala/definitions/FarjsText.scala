package definitions

import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsText extends ScalaJsModule {

  override val id = "farjs-text"

  override val base: File = file("text")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Text encodings and other text utils",

        coverageExcludedPackages := "farjs.text.raw",

        Compile / npmDependencies ++= Seq(
          "iconv-lite" -> "0.6.3"
        )
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition
  )
}
