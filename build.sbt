import definitions._
import scommons.sbtplugin.project.CommonModule
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

lazy val `farclone` = (project in file("."))
  .settings(CommonModule.settings: _*)
  .settings(FarcModule.settings: _*)
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
  `farclone-ui`,
  `farclone-app`
)

lazy val `farclone-ui` = FarcUi.definition
lazy val `farclone-app` = FarcApp.definition
