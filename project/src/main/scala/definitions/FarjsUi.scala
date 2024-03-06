package definitions

import sbt._
import scoverage.ScoverageKeys.coverageEnabled

object FarjsUi extends ScalaJsModule {

  override val id = "farjs-ui"

  override val base: File = file("ui")

  override def definition: Project = super.definition
    .settings(
      // disable coverage for JS-facade only module
      coverageEnabled := false
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil
}
