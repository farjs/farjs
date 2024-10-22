package definitions

import common.Libs
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonNodeJsModule
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsDao extends FarjsModule with CommonNodeJsModule {

  override val id = "farjs-dao"

  override val base: File = file("dao")

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
      .settings(
        description := "Data access object (DAO) module for FAR.js app",

        //to avoid Scala.js error: Found a dangling UndefinedParam
        coverageExcludedPackages := "farjs.domain.dao.HistoryDao"
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-websql", "scommons-websql-io", None)
    )
  }
  
  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsWebSqlIO.value
    )
  }
}
