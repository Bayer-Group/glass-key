name := "GlassKeyPlayClient"

version := "1.0"

lazy val `glasskeyplayclient` = (project in file(".")).enablePlugins(PlayScala)

resolvers += Opts.resolver.mavenLocalFile

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc ,
  anorm ,
  cache ,
  ws,
  "glasskey" %% "glass-key-play" % "0.1.18-SNAPSHOT")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
