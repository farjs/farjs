package definitions

import java.io.FileReader

import common.{Libs, TestLibs}
import definitions.ScalaJsModule._
import org.json.simple._
import sbt._
import scommons.sbtplugin.project.CommonClientModule

import scala.collection.JavaConverters._
import scala.collection.mutable
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JSON

trait ScalaJsModule extends NodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(CommonClientModule.settings: _*)
      .settings(
        requireJsDomEnv in Test := false,

        packageJsonSettings(scalaJSBundlerPackageJson, Compile),
        packageJsonSettings(scalaJSBundlerPackageJson, Test)
      )
  }

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = {
    super.superRepoProjectsDependencies ++ Seq(
      ("scommons-react", "scommons-react-core", None),
      ("scommons-react", "scommons-react-redux", None),

      ("scommons-react", "scommons-react-test", Some("test"))
    )
  }

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.runtimeDependencies.value ++ Seq(
      Libs.scommonsReactCore.value,
      Libs.scommonsReactRedux.value
    )
  }

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting {
    super.testDependencies.value ++ Seq(
      TestLibs.scommonsReactTest.value
    ).map(_ % "test")
  }
}

object ScalaJsModule {

  //copied definition of the task, since its defined as private at:
  //  scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
  //
  private val scalaJSBundlerPackageJson =
    TaskKey[BundlerFile.PackageJson]("scalaJSBundlerPackageJson",
      "Write a package.json file defining the NPM dependencies of project"
    )

  private def packageJsonSettings(task: TaskKey[BundlerFile.PackageJson], config: ConfigKey) = {
    task in config := {
      val packageJson = (task in config).value
      rewritePackageJson(packageJson.file)
      packageJson
    }
  }
  
  private def rewritePackageJson(packageJson: File): Unit = {
    val rawJson = JSONValue.parse(new FileReader(packageJson)).asInstanceOf[java.util.Map[String, Any]]
    val deps = rawJson.get("dependencies").asInstanceOf[java.util.Map[String, String]]
    if (deps != null) {
      deps.remove("react-dom")
      deps.remove("create-react-class")
    }
    
    IO.write(packageJson, mapToJSON(rawJson.asScala).toJson)
  }
  
  private def mapToJSON(obj: mutable.Map[String, Any]): JSON = {
    val fields = obj.toSeq.map { case (k, v) =>
      (k, anyToJSON(v))
    }
    
    JSON.obj(fields: _*)
  }

  private def anyToJSON(any: Any): JSON = any match {
    case v: JSONObject => mapToJSON(v.asInstanceOf[java.util.Map[String, Any]].asScala)
    case v: JSONArray => JSON.arr(v.asInstanceOf[java.util.List[Any]].asScala.map(anyToJSON): _*)
    case v: java.lang.String => JSON.str(v)
    case v: java.lang.Boolean => JSON.bool(v)
    case v =>
      throw new IllegalStateException(s"Unsupported JSON type: ${v.getClass.getName}")
  }
}
