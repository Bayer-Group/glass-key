// Use the Play sbt plugin for Play projects
logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.github.gseitz"  % "sbt-release"             % "1.0.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.typesafe.play"  %  "sbt-plugin"             % "2.3.6")

addSbtPlugin("com.typesafe.sbt"   %  "sbt-web"                % "1.0.2")

addSbtPlugin("org.scalastyle"     %% "scalastyle-sbt-plugin"  % "0.6.0")

