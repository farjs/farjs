package definitions

import common.TestLibs
import sbt._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarcNodeJs extends NodeJsModule {

  override val id = "farclone-nodejs"

  override val base: File = file("nodejs")

  override def definition: Project = super.definition
    .settings(
      coverageExcludedPackages := "scommons.nodejs.raw"
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override val superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Nil

  override val runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Nil)

  override val testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    TestLibs.scalaTestJs.value,
    TestLibs.scalaMockJs.value
  ).map(_ % "test"))
}
