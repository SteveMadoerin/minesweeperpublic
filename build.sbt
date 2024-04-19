import org.scoverage.coveralls.GitHubActions

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "de.htwg.sa"
ThisBuild / organizationName := "minesweeper"


lazy val root: Project = project
    .in(file("."))
    .dependsOn(controller, model, persistence, ui)
    .aggregate(controller, model, persistence, ui)
    .settings(
        name := "Minesweeper",
        commonSettings,
        jacocoCoverallsCoverageSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)

lazy val model = project
    .in(file("model"))
    .settings(
        name := "model",
        commonSettings
    )

lazy val persistence = project
    .in(file("persistence"))
    .dependsOn(model)
    .settings(
        name := "persistence",
        commonSettings
    )

lazy val controller = project
    .in(file("controller"))
    .dependsOn(model, persistence)
    .settings(
        name := "controller",
        commonSettings
    )

lazy val ui = project
    .in(file("ui"))
    .dependsOn(model, persistence, controller)
    .settings(
        name := "ui",
        commonSettings,
    )


lazy val commonSettings = Seq(
    libraryDependencies ++= Seq(
        ("com.typesafe.play" %% "play-json" % "2.10.0-RC5"),
         "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
        "net.codingwell" %% "scala-guice" % "7.0.0",
        "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
        "org.scalactic" %% "scalactic" % "3.2.16",
        ("org.scalatest" %% "scalatest" % "3.2.16" % "test"),
        "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",
        "com.typesafe.akka" %% "akka-stream" % "2.8.0" ,
        "com.typesafe.akka" %% "akka-http" % "10.5.0"
    )
)

lazy val jacocoCoverallsCoverageSettings = Seq(
        jacocoExcludes := Seq(
            "*gui.*"
        ),
        jacocoCoverallsServiceName := "github-actions",
        jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
        jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
        jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN"),
)


import org.scoverage.coveralls.Imports.CoverallsKeys.*

coverallsTokenFile := sys.env.get("COVERALLS_REPO_TOKEN")
coverallsService := Some(GitHubActions)