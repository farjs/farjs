package definitions

import sbt.Keys._
import sbt._

object FarjsFs extends ScalaJsModule {

  override val id = "farjs-fs"

  override val base: File = file("fs")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Local file system filelist api implementation"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test"
  )
}
