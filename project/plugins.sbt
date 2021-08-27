//resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

//addSbtPlugin(("org.scommons.sbt" % "sbt-scommons-plugin-sjs06" % "0.6.0-SNAPSHOT").changing())
addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin-sjs06" % "0.6.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")
