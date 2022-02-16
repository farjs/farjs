package definitions

import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsFileList extends ScalaJsModule {

  override val id = "farjs-filelist"
  
  override val base: File = file("filelist")

  override def definition: Project = super.definition
    .settings(
      coverageExcludedPackages := "farjs.filelist.fs.FSFileListActions"
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition
  )
}
