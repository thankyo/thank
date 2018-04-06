import play.sbt.routes.RoutesKeys
import sbt.Keys.{libraryDependencies, resolvers}

import scala.io.Source

name := "backend"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.5"
scalacOptions ++= Seq("-unchecked", "-deprecation")

val silhouetteVersion = "5.0.3"
val reactMongoVersion = "0.13.0"
val scalaGuiceVersion = "4.2.0"
val elastic4sVersion = "6.1.3"

val commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := "2.12.5",
  libraryDependencies ++= Seq(
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion
  )
)

resolvers += Resolver.mavenCentral
resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/content/repositories/atlassian-public/"
resolvers += Resolver.sbtPluginRepo("releases")

lazy val common = (project in file("./common")).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      ws,

      "net.minidev" % "json-smart" % "2.3",
      "com.mohiva" %% "play-silhouette" % silhouetteVersion excludeAll (
        ExclusionRule(organization = "com.typesafe.play")
      ),

      "com.typesafe.play" %% "play" % "2.6.12" % "provided",

      "org.reactivemongo" %% "reactivemongo" % reactMongoVersion,
      "org.reactivemongo" %% "play2-reactivemongo" % s"${reactMongoVersion}-play26",
      "org.reactivemongo" %% "reactivemongo-akkastream" % reactMongoVersion
    )
  )


lazy val auth = (project in file("./auth")).
  enablePlugins(PlayScala).
  dependsOn(common).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      ws,

      "org.matthicks" %% "mailgun4s" % "1.0.9",

      "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
      "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
      "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,

      "com.iheart" %% "ficus" % "1.4.3",
    )
  )

lazy val user = (project in file("./user")).
  enablePlugins(PlayScala).
  dependsOn(common).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      ws
    )
  )

lazy val thank = (project in file("./thank")).
  enablePlugins(PlayScala).
  dependsOn(common).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      ws,
      "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
      "org.jsoup" % "jsoup" % "1.11.2",
    )
  )

lazy val payment = (project in file("./payment")).
  enablePlugins(PlayScala).
  dependsOn(common).
  settings(commonSettings).
  settings(
    libraryDependencies ++= Seq(
      ws,
      "com.stripe" % "stripe-java" % "5.33.3",
      "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion
    )
  )

lazy val root = (project in file(".")).
  enablePlugins(PlayScala).
  enablePlugins(DockerPlugin).
  dependsOn(common, auth, payment, thank, user).
  aggregate(common, auth, payment, thank, user)


libraryDependencies ++= Seq(
  guice,
  ws,

  "com.iheart" %% "ficus" % "1.4.3",

  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,

  "io.sentry" % "sentry-logback" % "1.7.2",

  "org.apache.commons" % "commons-text" % "1.2" % Test,
  specs2 % Test
)

TwirlKeys.templateImports := Seq()

def readProperties(fileName: String): Map[String, String] = {
  val f = new File(fileName)
  if (!f.exists()) {
    return Map.empty[String, String]
  }

  Source.fromFile(f).
    getLines().
    map(_.trim()).
    filterNot(_.isEmpty).
    map(line => {
      val parts = line.split("=")
      (parts(0).trim(), parts(1).trim())
    }).
    toList.
    toMap[String, String]
}

envVars ++= readProperties("./local.properties")

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")
coverageExcludedFiles := """.*\.template\.scala;.*JavaScriptReverseRoutes.*;.*ReverseRoutes.*;.*Routes.*;.*Module.*;"""

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf")

version in Docker := "latest"
maintainer in Docker := "antono@loveit.tips"
dockerBaseImage := "openjdk:8u162-jre-slim"
dockerRepository := Some("loveit")
dockerExposedPorts := Seq(9000)

