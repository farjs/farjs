package definitions

import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsUi extends ScalaJsModule {

  override val id = "farjs-ui"

  override val base: File = file("ui")

  override def definition: Project = super.definition
    .settings(
      coverageExcludedPackages :=
        "scommons.react.blessed.raw" +
          ";farjs.ui.Exports" +
          ";farjs.ui.raw"
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil
}
