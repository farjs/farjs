package definitions

import sbt.Keys._
import sbt._

object FarjsArchiver extends ScalaJsModule {

  override val id = "farjs-archiver"

  override val base: File = file("archiver")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Multiple archivers filelist api implementation"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test"
  )
}
