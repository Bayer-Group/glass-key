import sbtrelease.ReleasePlugin._

val theOrg = "glasskey"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

organization := theOrg

val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization      := theOrg,
  scalaVersion      := scalaVersion.value,
  publishMavenStyle := true,
  scalacOptions     := Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xcheckinit",
    "-Xlint",
    "-Xverify",
    "-Yclosure-elim",
    "-Yinline",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-encoding",
    "utf8"),
  resolvers ++= Seq(
    Opts.resolver.mavenLocalFile,
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")
)


val akkaV = "2.3.6"
val sprayV = "1.3.2"
val sprayJsonV = "1.3.1"

val commonDeps = Seq[ModuleID](
  "com.fasterxml.jackson.module"  %% "jackson-module-scala" % "2.4.4",
  "commons-codec"                 %  "commons-codec"        % "1.6",
  "com.nimbusds"                  %  "nimbus-jose-jwt"      % "3.8.2",
  "com.typesafe"                  %  "config"               % "1.2.1",
  "com.typesafe.slick"            %% "slick"                % "2.1.0",
  "org.scalatest"                 %% "scalatest"            % "2.2.1"   % "test"
)

def scalaVerDependency(scalaVersion: String) = scalaVersion match {
  case "2.10.4" => Seq[ModuleID]("org.scalatest" % "scalatest_2.10" % "2.2.1" % "test")
  case "2.11.4" => Seq[ModuleID]("org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "org.scala-lang.modules"        %% "scala-parser-combinators" % "1.0.3")
}

val playDeps =
  Seq[ModuleID] (
    jdbc, anorm, cache, ws
  )

val sprayDeps =
  Seq[ModuleID] (
    "io.spray"          %% "spray-can"      % sprayV,
    "io.spray"          %% "spray-routing"  % sprayV,
    "io.spray"          %% "spray-client"   % sprayV,
    "io.spray"          %% "spray-caching"  % sprayV,
    "io.spray"          %% "spray-json"     % sprayJsonV,
    "com.typesafe.akka" %% "akka-actor"     % akkaV,
    "io.spray"          %% "spray-testkit"  % sprayV      % "test",
    "com.typesafe.akka" %% "akka-testkit"   % akkaV       % "test"
  )

lazy val common = (project in file("glass-key-common")).
  settings(commonSettings: _*).
  settings(name := "glass-key-common").
  settings(libraryDependencies ++= commonDeps).
  settings(libraryDependencies ++= scalaVerDependency(scalaVersion.value)).
  settings(libraryDependencies ++= Seq[ModuleID]("org.scala-lang" % "scala-reflect" % scalaVersion.value))

lazy val play = (project in file("glass-key-play")).
  settings(commonSettings: _*).
  settings(name := "glass-key-play").
  settings(libraryDependencies ++= playDeps).
  settings(libraryDependencies ++= Seq[ModuleID]("org.scala-lang" % "scala-reflect" % scalaVersion.value)).
  settings(libraryDependencies ++= scalaVerDependency(scalaVersion.value)).
  dependsOn(common)

lazy val spray = (project in file("glass-key-spray")).
  settings(commonSettings: _*).
  settings(name := "glass-key-spray").
  settings(libraryDependencies ++= sprayDeps).
  settings(libraryDependencies ++= scalaVerDependency(scalaVersion.value)).
  settings(libraryDependencies ++= Seq[ModuleID]("org.scala-lang" % "scala-reflect" % scalaVersion.value)).
  dependsOn(common)
