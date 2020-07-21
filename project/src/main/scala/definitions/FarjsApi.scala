package definitions

import sbt._

object FarjsApi extends NodeJsModule {

  override val id = "farjs-api"
  
  override val base: File = file("api")

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil
}
