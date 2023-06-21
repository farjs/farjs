package definitions

import sbt._

object FarjsFileList extends ScalaJsModule {

  override val id = "farjs-filelist"
  
  override val base: File = file("filelist")

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsUi.definition % "compile->compile;test->test"
  )
}
