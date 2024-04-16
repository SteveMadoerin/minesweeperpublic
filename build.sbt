import org.scoverage.coveralls.GitHubActions

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "de.htwg.sa"
ThisBuild / organizationName := "minesweeper"


lazy val root: Project = (project in file(""))
    .dependsOn(controller, model, persistence, ui, shared)
    .aggregate(controller, model, persistence, ui, shared)
    .settings(
        name := "Minesweeper",
        version:= "0.1.0-SNAPSHOT",
        commonSettings
    )


lazy val shared = (project in file("shared"))
    .settings(
        name := "shared",
        version:= "0.1.0-SNAPSHOT",
        commonSettings
    )

lazy val model = (project in file("model"))
    .dependsOn(shared)
    .settings(
        name := "model",
        version:= "0.1.0-SNAPSHOT",
        commonSettings
    )

lazy val persistence = (project in file("persistence"))
    .dependsOn(model, shared)
    .aggregate(model, shared)
    .settings(
        name := "persistence",
        version:= "0.1.0-SNAPSHOT",
        commonSettings
    )

lazy val controller = (project in file("controller"))
    .dependsOn(model, persistence, shared)
    .aggregate(model, persistence, shared)
    .settings(
        name := "controller",
        version:= "0.1.0-SNAPSHOT",
        commonSettings
    )

lazy val ui = (project in file("ui"))
    .dependsOn(model, persistence, controller, shared)
    .aggregate(model, persistence, controller, shared)
    .settings(
        name := "ui",
        commonSettings,
    )


lazy val commonSettings = Seq(
    libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.0-RC5"),
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
    libraryDependencies +="net.codingwell" %% "scala-guice" % "7.0.0",
    libraryDependencies +="org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies +="org.scalactic" %% "scalactic" % "3.2.16",
    libraryDependencies +=("org.scalatest" %% "scalatest" % "3.2.16" % "test")
)

/* lazy val jacocoCoverallsCoverageSettings = Seq(
        jacocoExcludes := Seq(
            "*gui.*"
        ),
        jacocoCoverallsServiceName := "github-actions",
        jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
        jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
        jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN"),
) */


import org.scoverage.coveralls.Imports.CoverallsKeys.*

coverallsTokenFile := sys.env.get("COVERALLS_REPO_TOKEN")
coverallsService := Some(GitHubActions)
coverageExcludedPackages := "*gui.*"