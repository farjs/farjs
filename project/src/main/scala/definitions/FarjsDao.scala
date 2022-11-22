package definitions

import common.Libs
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonNodeJsModule

object FarjsDao extends FarjsModule with CommonNodeJsModule {

  override val id = "farjs-dao"

  override val base: File = file("dao")

  override def definition: Project = {
    super.definition
      .settings(
        description := "Data access object (DAO) module for FAR.js app",

        Compile / npmDependencies ++= Seq(
          "websql" -> "2.0.3"
        )
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-websql", "scommons-websql-migrations", None),
      ("scommons-websql", "scommons-websql-io", None)
    )
  }
  
  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsWebSqlMigrations.value,
      Libs.scommonsWebSqlIO.value
    )
  }
}
