package definitions

import sbt.Keys._
import sbt._

object FarjsCopyMove extends ScalaJsModule {

  override val id = "farjs-copymove"

  override val base: File = file("copymove")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Implementation of copy and move for filelist"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test"
  )
}
