val scala3Version = "3.3.1"

lazy val root = project
    .in(file("."))
    .dependsOn(core, model, persistence, ui)
    .settings(
        name := "Minesweeper",
        version := "0.1.0-SNAPSHOT",
        sharedSettings,
        jacocoCoverallsCoverageSettings
    )
    .aggregate(core, model, persistence, ui)
    .enablePlugins(JacocoCoverallsPlugin)

lazy val core = project
    .in(file("core"))
    .dependsOn(model, persistence)
    .settings(
        name := "core",
        sharedSettings
    )

lazy val model = project
    .in(file("model"))
    .settings(
        name := "model",
        sharedSettings
    )

lazy val persistence = project
    .in(file("persistence"))
    .dependsOn(model) // evt ui
    .settings(
        name := "persistence",
        sharedSettings
    )

lazy val ui = project
    .in(file("ui"))
    .dependsOn(core, model, persistence)
    .settings(
        name := "ui",
        sharedSettings
    )

lazy val sharedSettings: Seq[Def.Setting[?]] = Seq(
    scalaVersion := scala3Version,
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
