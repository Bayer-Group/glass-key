name := """GlassKeyPlayResource"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "glasskey" %% "glass-key-play" % "0.1.18-SNAPSHOT"
)
