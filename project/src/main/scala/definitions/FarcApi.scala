package definitions

import sbt._

object FarcApi extends NodeJsModule {

  override val id = "farclone-api"
  
  override val base: File = file("api")

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil
}
