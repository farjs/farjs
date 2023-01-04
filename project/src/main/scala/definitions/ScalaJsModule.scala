package definitions

import common.{Libs, TestLibs}
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.project.CommonNodeJsModule

trait ScalaJsModule extends FarjsModule with CommonNodeJsModule {

  override def definition: Project = {
    super.definition
      .settings(ScalaJsModule.settings: _*)
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

  private val scalaJSBundlerPackageJson =
    TaskKey[BundlerFile.PackageJson]("scalaJSBundlerPackageJson",
      "Write a package.json file defining the NPM dependencies of project",
      KeyRanks.Invisible
    )

  val settings: Seq[Setting[_]] = Seq(
    removeWebpackDevDependencies(Compile, scalaJSBundlerPackageJson),
    removeWebpackDevDependencies(Test, scalaJSBundlerPackageJson)
  )

  private def removeWebpackDevDependencies(config: ConfigKey, packageJsonTask: TaskKey[BundlerFile.PackageJson]) = {
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.databind.node._

    import scala.collection.JavaConverters._

    config / packageJsonTask := {
      val packageJsonFile = (config / packageJsonTask).value
      val mapper = new ObjectMapper()
      val json = mapper.readTree(IO.read(packageJsonFile.file)).asInstanceOf[ObjectNode]
      val devDeps = json.get("devDependencies").asInstanceOf[ObjectNode]
      devDeps.remove(List(
        "source-map-loader",
        "webpack",
        "concat-with-sourcemaps",
        "webpack-cli",
        "webpack-dev-server"
      ).asJava)

      mapper.writerWithDefaultPrettyPrinter().writeValue(packageJsonFile.file, json)

      packageJsonFile
    }
  }
}
