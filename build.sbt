import definitions._
import scommons.sbtplugin.project.CommonModule
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

lazy val `farjs` = (project in file("."))
  .settings(CommonModule.settings: _*)
  .settings(FarjsModule.settings: _*)
  .settings(
    skip in publish := true,
    publish := ((): Unit),
    publishLocal := ((): Unit),
    publishM2 := ((): Unit)
  )
  .settings(
    ideExcludedDirectories += baseDirectory.value / "docs" / "_site"
  )
  .aggregate(
    `farjs-api`,
    `farjs-ui`,
    `farjs-app`
  )

lazy val `farjs-api` = FarjsApi.definition
lazy val `farjs-ui` = FarjsUi.definition
lazy val `farjs-app` = FarjsApp.definition
