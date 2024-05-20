ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

name := "persistence"

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
  libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
  libraryDependencies += "com.typesafe.slick" %% "slick" % "3.5.1",
  libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4",
  libraryDependencies += "org.postgresql" % "postgresql" % "42.7.3",
  libraryDependencies += ("org.mongodb.scala" %% "mongo-scala-driver" % "4.3.3").cross(CrossVersion.for3Use2_13)
)

lazy val persistence = (project in file("."))
  .settings(
    commonSettings
  )