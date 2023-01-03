package definitions

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.BundlingMode
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
          ";farjs.app.raw" +
          ";farjs.app.filelist.zip.ZipApi", // avoid "Found a dangling UndefinedParam" during test with coverage

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
      scommonsRequireWebpackInTest := true,
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
    FarjsFs.definition,
    FarjsDao.definition
  )
}
