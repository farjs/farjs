package definitions

import common.Libs
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object FarjsDao extends FarjsModule {

  override val id = "farjs-dao"

  override val base: File = file("dao")

  override def definition: Project = super.definition
    .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
    .settings(
      description := "Data access object (DAO) module for FAR.js app",

      Compile / npmDependencies ++= Seq(
        "websql" -> "2.0.3"
      )
    )

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-websql", "scommons-websql-migrations", None),
    ("scommons-websql", "scommons-websql-io", None)
  )
  
  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    Libs.scommonsWebSqlMigrations.value,
    Libs.scommonsWebSqlIO.value
  ))
  
  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Nil)
}
