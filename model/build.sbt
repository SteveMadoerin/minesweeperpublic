ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

name := "model"

lazy val commonSettings = Seq(
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.0-RC5"),
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
  libraryDependencies +="net.codingwell" %% "scala-guice" % "7.0.0",
  libraryDependencies +="org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  libraryDependencies +="org.scalactic" %% "scalactic" % "3.2.16",
  libraryDependencies +=("org.scalatest" %% "scalatest" % "3.2.16" % "test"),
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.5",
)

lazy val model = (project in file("."))
  .settings(
    commonSettings
  )
