ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "de.htwg.sa.minesweeper"
ThisBuild / organizationName := "minesweeper"





lazy val shared = project
    .in(file("shared"))
    .settings(
        name := "shared",
        commonSettings
    )

lazy val model = project
    .in(file("model"))
    .dependsOn(shared)
    .settings(
        name := "model",
        commonSettings
    )

lazy val core = project
    .in(file("core"))
    .dependsOn(model, persistence, shared)
    .settings(
        name := "core",
        commonSettings
    )


lazy val persistence = project
    .in(file("persistence"))
    .dependsOn(model, shared)
    .settings(
        name := "persistence",
        commonSettings
    )

lazy val ui = project
    .in(file("ui"))
    .dependsOn(model, persistence, core, shared)
    .settings(
        name := "ui",
        commonSettings
    )

lazy val root: Project = project
    .in(file("."))
    .dependsOn(core, model, persistence, ui, shared)
    .settings(
        commonSettings,
        jacocoCoverallsCoverageSettings
    )
    .aggregate(core, model, persistence, ui, shared)
    .enablePlugins(JacocoCoverallsPlugin)


lazy val commonSettings: Seq[Def.Setting[?]] = Seq(
    libraryDependencies ++= Seq(
        ("com.typesafe.play" %% "play-json" % "2.10.0-RC5"),
         "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
        "net.codingwell" %% "scala-guice" % "7.0.0",
        "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
        "org.scalactic" %% "scalactic" % "3.2.16",
        ("org.scalatest" %% "scalatest" % "3.2.16" % "test")
    )
)

lazy val jacocoCoverallsCoverageSettings: Seq[Def.Setting[?]] = Seq(
        jacocoExcludes := Seq(
            "*gui.*"
        ),
        jacocoCoverallsServiceName := "github-actions",
        jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
        jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
        jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN")
)
