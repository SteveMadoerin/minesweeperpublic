val scala3Version = "3.3.1"

lazy val root = project
    .in(file("."))
    .settings(
        name := "Minesweeper",
        version := "0.1.0-SNAPSHOT",
        
        scalaVersion := scala3Version,
        
        //libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
        libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.0-RC5"),
        libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
        libraryDependencies += "net.codingwell" %% "scala-guice" % "7.0.0",
        libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
        //libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31",
        libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16",
        libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test",
        jacocoExcludes := Seq(
            "*gui.*"
        ),
        jacocoCoverallsServiceName := "github-actions",
        jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
        jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
        jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN")
    )
    .enablePlugins(JacocoCoverallsPlugin)
