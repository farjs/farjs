package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.{BundlingMode, NpmPackage, Webpack}
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

      coverageExcludedPackages := "farjs.app.FarjsApp",

      //TODO: temporarily disabled
      //  @see: https://github.com/scalameta/metabrowse/issues/271
      fullOptJS / scalaJSLinkerConfig ~= {
        _.withOptimizer(false)
      },

      scalaJSUseMainModuleInitializer := false,
      webpackBundlingMode := BundlingMode.Application,

      webpackResources := {
        baseDirectory.value / ".." / "LICENSE.txt" +++
          baseDirectory.value / ".." / "README.md"
      },
      
      //dev
      fastOptJS / webpackConfigFile := Some(
        baseDirectory.value / "src" / "main" / "resources" / "dev.webpack.config.js"
      ),
      //prod
      fullOptJS / webpackConfigFile := Some(
        baseDirectory.value / "src" / "main" / "resources" / "prod.webpack.config.js"
      ),
      //tests
      scommonsRequireWebpackInTest := false,
      Test / fastOptJS := {
        val logger = streams.value.log
        val bundleOutput = (Test / fastOptJS).value
        val targetDir = bundleOutput.data.getParentFile

        val customWebpackConfigFile = (Test / webpackConfigFile).value
        val nodeArgs = (Test / webpackNodeArgs).value
        val bundleName = bundleOutput.data.name.stripSuffix(".js")
        val webpackOutput = targetDir / s"$bundleName-webpack-out.js"
        val webpackVersion = (webpack / version).value

        logger.info("Executing webpack...")
        val loader = bundleOutput.data
        val configArgs = customWebpackConfigFile match {
          case Some(configFile) =>
            val customConfigFileCopy = Webpack.copyCustomWebpackConfigFiles(targetDir, webpackResources.value.get)(configFile)
            Seq("--config", customConfigFileCopy.getAbsolutePath)
          case None =>
            Seq.empty
        }

        val allArgs = Seq(
          "--entry", loader.absolutePath,
          "--output-path", targetDir.absolutePath,
          "--output-filename", webpackOutput.name
        ) ++ configArgs

        NpmPackage(webpackVersion).major match {
          case Some(5) =>
            Webpack.run(nodeArgs: _*)(allArgs: _*)(targetDir, logger)
          case Some(x) =>
            sys.error(s"Unsupported webpack major version $x")
          case None =>
            sys.error("No webpack version defined")
        }
        Attributed(webpackOutput)(bundleOutput.metadata)
      },
      Test / webpackConfigFile := Some(
        baseDirectory.value / "src" / "main" / "resources" / "test.webpack.config.js"
      ),

      //useYarn := true,
      //yarnExtraArgs := Seq("--frozen-lockfile"),

      Compile / npmDevDependencies ++= Seq(
        "webpack-merge" -> "5.8.0",
        "webpack-node-externals" -> "3.0.0"
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
    FarjsDao.definition
  )
}
