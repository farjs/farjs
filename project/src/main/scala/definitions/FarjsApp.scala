package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages

object FarjsApp extends ScalaJsModule {

  override val id = "farjs-app"

  override val base: File = file("app")

  override def definition: Project = super.definition
    .settings(
      publish / skip := true,
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishM2 := ((): Unit),

      scommonsBundlesFileFilter := "*.sql",

      coverageExcludedPackages :=
        "farjs.app.FarjsApp" +
          ";farjs.app.raw",

      //TODO: temporarily disabled
      //  @see: https://github.com/scalameta/metabrowse/issues/271
//      fullOptJS / scalaJSLinkerConfig ~= {
//        _.withOptimizer(false)
//      },

      //TODO: temporarily disabled to avoid "Cannot use multiple modules with the Closure Compiler"
      //  @see: https://github.com/scala-js/scala-js/issues/3893
      Compile / fullLinkJS / scalaJSLinkerConfig ~= {
        _.withClosureCompiler(false)
      },

      scalaJSUseMainModuleInitializer := false,

      // our custom mocks during test
      scommonsNodeJsTestLibs := Seq("test.aliases.js"),

      Compile / fullLinkJS := {
        val (modules, report) = ScalaJsModule.moveModules(
          linkerDir = (Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value,
          targetDir = (Compile / npmUpdate / crossTarget).value,
          report = (Compile / fullLinkJS).value
        )
        val packageJson = (Compile / ScalaJsModule.scalaJSBundlerPackageJson).value
        ScalaJsModule.addFilesToPackageJson(packageJson.file, modules)
        report
      },
      Compile / fastLinkJS := {
        val (_, report) = ScalaJsModule.moveModules(
          linkerDir = (Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value,
          targetDir = (Compile / npmUpdate / crossTarget).value,
          report = (Compile / fastLinkJS).value
        )
        report
      },

      Compile / npmUpdate := {
        def copyToWorkingDir(targetDir: File)(file: File): File = {
          val copy = targetDir / file.name
          IO.copyFile(file, copy)
          copy
        }
      
        val targetDir = (Compile / npmUpdate).value
        copyToWorkingDir(targetDir)(baseDirectory.value / ".." / "LICENSE.txt")
        copyToWorkingDir(targetDir)(baseDirectory.value / ".." / "README.md")
        targetDir
      },

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),

      Compile / npmDependencies ++= Seq(
        "module-alias" -> "2.2.2"
      ),

      Compile / npmDevDependencies ++= Seq(
        "rollup" -> "^3.9.1",
        "@rollup/plugin-commonjs" -> "^24.0.0",
        "@rollup/plugin-node-resolve" -> "^15.0.1",
        "@rollup/plugin-replace" -> "^5.0.2",
        "@ampproject/rollup-plugin-closure-compiler" -> "^0.27.0"
      ),

      Compile / additionalNpmConfig := {
        import com.fasterxml.jackson.databind.node._
        import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
        import scalajsbundler.util.JSON
        import scalajsbundler.util.JSON._

        import scala.collection.JavaConverters._
        
        def convertObjectNode(json: ObjectNode): List[(String, JSON)] = {
          json.fields().asScala.toList.map { entry =>
            val key = entry.getKey
            (key, convertJsonNode(key, entry.getValue))
          }
        }
        
        def convertJsonNode(key: String, value: JsonNode): JSON = {
          if (value.isBoolean) bool(value.asBoolean())
          else if (value.isTextual) str(value.asText())
          else if (value.isArray) {
            arr(value.asInstanceOf[ArrayNode].elements().asScala.toList.map(j => convertJsonNode(key, j)): _*)
          }
          else if (value.isObject) {
            obj(convertObjectNode(value.asInstanceOf[ObjectNode]): _*)
          }
          else sys.error(s"Unknown package.json field: $key")
        }
        
        val mapper = new ObjectMapper()
        val packageJson = IO.read(baseDirectory.value / ".." / "package.json")
        val json = mapper.readTree(packageJson).asInstanceOf[ObjectNode]
        convertObjectNode(json.remove(List("dependencies", "devDependencies").asJava)).toMap ++ Map(
          "version" -> str(version.value)
        )
      }
    )

  override val internalDependencies: Seq[ClasspathDep[ProjectReference]] = Seq(
    FarjsFileList.definition % "compile->compile;test->test",
    FarjsFs.definition,
    FarjsArchiver.definition,
    FarjsDao.definition
  )
}
