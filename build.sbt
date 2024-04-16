ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "de.htwg.sa"
ThisBuild / organizationName := "minesweeper"


lazy val root: Project = project
    .in(file("."))
    .dependsOn(controller, model, persistence, ui, shared)
    .aggregate(controller, model, persistence, ui, shared)
    .settings(
        name := "Minesweeper",
        commonSettings,
        jacocoCoverallsCoverageSettings
        //jacocoCoverallsReportSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)
    .settings(
        publish := {},
        publishLocal := {},
        test := {},
        testOnly := {}
    )


lazy val shared = project
    .in(file("shared"))
    .settings(
        name := "shared",
        commonSettings,
        jacocoCoverallsCoverageSettings
    )

lazy val model = project
    .in(file("model"))
    .dependsOn(shared)
    .settings(
        name := "model",
        commonSettings,
        jacocoCoverallsCoverageSettings
        //jacocoCoverallsCoverageSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)

lazy val persistence = project
    .in(file("persistence"))
    .dependsOn(model, shared)
    .settings(
        name := "persistence",
        commonSettings,
        jacocoCoverallsCoverageSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)

lazy val controller = project
    .in(file("controller"))
    .dependsOn(model, persistence, shared)
    .settings(
        name := "controller",
                commonSettings,
        jacocoCoverallsCoverageSettings
    )
    .enablePlugins(JacocoCoverallsPlugin)

lazy val ui = project
    .in(file("ui"))
    .dependsOn(model, persistence, controller, shared)
    .settings(
        name := "ui",
        commonSettings,
        jacocoCoverallsCoverageSettings
    )
    .enablePlugins(JacocoPlugin, JacocoCoverallsPlugin)




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
        jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN"),
)


