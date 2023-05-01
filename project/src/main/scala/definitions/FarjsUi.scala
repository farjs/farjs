package definitions

import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarjsUi extends ScalaJsModule {

  override val id = "farjs-ui"

  override val base: File = file("ui")

  override def definition: Project = super.definition
    .settings(
      coverageExcludedPackages :=
        "scommons.react.blessed.raw" +
          ";farjs.ui.Exports",

      Compile / npmDependencies ++= Seq(
        //"blessed" -> "0.1.81",
        "@farjs/blessed" -> "0.2.6",
        //"neo-blessed" -> "0.2.0",
        //"@medv/blessed" -> "2.0.0",
        "react-blessed" -> "0.7.2"
        //"react-reconciler" -> "0.20.4"
      )
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil
}
