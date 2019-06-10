package definitions

import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarcUi extends ScalaJsModule {

  override val id = "farclone-ui"

  override val base: File = file("ui")

  override def definition: Project = super.definition
    .settings(
      coverageExcludedPackages := "scommons.react.blessed.raw",

      npmDependencies in Compile ++= Seq(
        "blessed" -> "0.1.81",
        //"neo-blessed" -> "0.2.0",
        //"@medv/blessed" -> "2.0.0",
        "react-blessed" -> "0.6.0"
        //"react-reconciler" -> "0.20.4"
      )
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarcApi.definition
  )
}
