ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

name := "ui"

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
  libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4",
  libraryDependencies += "io.circe" %% "circe-core" % "0.14.1",
  libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1",
  libraryDependencies += "io.circe" %% "circe-parser" % "0.14.1",
  libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.8.0",
  libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.8.0",
  libraryDependencies += ("org.apache.kafka" %% "kafka-streams-scala" % "3.7.0").cross(CrossVersion.for3Use2_13),
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