ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

name := "ui"

lazy val commonSettings = Seq(
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.5"),
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
  libraryDependencies +="net.codingwell" %% "scala-guice" % "7.0.0",
  libraryDependencies +="org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  libraryDependencies +="org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies +=("org.scalatest" %% "scalatest" % "3.2.18" % "test"),
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  libraryDependencies += "org.slf4j" % "slf4j-nop" % "2.0.13",
)

lazy val ui = (project in file("."))
  .settings(
    commonSettings,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )

scalacOptions ++= Seq("-deprecation", "-feature")

assembly / target := baseDirectory.value