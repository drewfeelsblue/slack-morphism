import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.DefaultReadableGit
import microsites._
import sbt.Package.ManifestAttributes

name := "slack-morphism-root"

ThisBuild / version := "1.0.0-SNAPSHOT"

ThisBuild / description := "Type-Safe Reactive Client and Blocks Templating for Slack"

ThisBuild / organization := "org.latestbit"

ThisBuild / homepage := Some( url( "https://slack.abdolence.dev" ) )

ThisBuild / licenses := Seq(
  ( "Apache License v2.0", url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) )
)

ThisBuild / crossScalaVersions := Seq( "2.13.1", "2.12.10" )

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / sbtVersion := "1.3.5"

ThisBuild / scalacOptions ++= Seq( "-feature" )

ThisBuild / exportJars := true

publishArtifact := false

publishTo := Some( Resolver.file( "Unused transient repository", file( "target/unusedrepo" ) ) )

ThisBuild / resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
) ++ (CrossVersion.partialVersion( (ThisBuild / scalaVersion).value ) match {
  case Some( ( 2, n ) ) if n >= 13 => Seq( "-Xsource:2.14" )
  case Some( ( 2, n ) ) if n < 13  => Seq( "-Ypartial-unification" )
  case _                           => Seq()
})

ThisBuild / javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-source",
  "1.8",
  "-target",
  "1.8",
  "-Xlint"
)

ThisBuild / packageOptions := Seq(
  ManifestAttributes(
    ( "Build-Jdk", System.getProperty( "java.version" ) ),
    (
      "Build-Date",
      ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_OFFSET_DATE_TIME )
    )
  )
)

val catsVersion = "2.0.0"
val circeVersion = "0.12.3"
val scalaCollectionsCompatVersion = "2.1.3"
val sttpVersion = "2.0.0-RC5"
val circeAdtCodecVersion = "0.6.1"
val reactiveStreamsVersion = "1.0.3"

// For tests
val scalaTestVersion = "3.1.0"
val scalaCheckVersion = "1.14.3"
val scalaTestPlusCheck = "3.1.0.0-RC2"
val scalaTestPlusTestNG = "3.1.0.0" // for reactive publisher tck testing
val scalaCheckShapeless = "1.2.3"

// For full-featured examples we use additional libs like akka-http
val akkaVersion = "2.5.27"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.30.0"
val logbackVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val scoptVersion = "3.7.1"
val swayDbVersion = "0.11"

val baseDependencies =
  Seq(
    "org.typelevel" %% "cats-core"
  ).map( _ % catsVersion ) ++
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map( _ % circeVersion ) ++
    Seq(
      "org.latestbit" %% "circe-tagged-adt-codec" % circeAdtCodecVersion
    ) ++
    Seq(
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "cats-testkit" % catsVersion,
      "org.reactivestreams" % "reactive-streams-tck" % reactiveStreamsVersion,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusCheck,
      "org.scalatestplus" %% "testng-6-7" % scalaTestPlusTestNG,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapeless
    ).map( _ % "test" )

//addCompilerPlugin( "org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full )

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val slackMorphismRoot = project
  .in( file( "." ) )
  .aggregate( slackMorphismModels, slackMorphismClient, slackMorphismExamples )
  .settings( noPublishSettings )

lazy val slackMorphismModels =
  (project in file( "models" )).settings(
    name := "slack-morphism-models",
    libraryDependencies ++= baseDependencies ++ Seq()
  )

lazy val slackMorphismClient =
  (project in file( "client" ))
    .settings(
      name := "slack-morphism-client",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.softwaremill.sttp.client" %% "core" % sttpVersion,
        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionsCompatVersion,
        "org.reactivestreams" % "reactive-streams" % reactiveStreamsVersion
      )
    )
    .dependsOn( slackMorphismModels )

lazy val slackMorphismExamples =
  (project in file( "examples/akka-http" ))
    .settings(
      name := "slack-morphism-akka",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.github.scopt" %% "scopt" % scoptVersion,
        "ch.qos.logback" % "logback-classic" % logbackVersion,
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion
          excludeAll (ExclusionRule( organization = "com.typesafe.akka" ) ),
        "com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion,
        "io.swaydb" %% "swaydb" % swayDbVersion
      )
    )
    .dependsOn( slackMorphismClient )

lazy val docSettings = Seq(
  micrositeName := "Slack Morphism for Scala",
  micrositeUrl := "https://slack.abdolence.dev",
  micrositeDocumentationUrl := "/docs",
  micrositeDocumentationLabelDescription := "Docs",
  micrositeAuthor := "Abdulla Abdurakhmanov",
  micrositeHomepage := "https://slack.abdolence.dev",
  micrositeOrganizationHomepage := "https://abdolence.dev",
  micrositeGithubOwner := "abdolence",
  micrositeGithubRepo := "slack-morphism",
  micrositePushSiteWith := GitHub4s,
  autoAPIMappings := true,
  //micrositeTheme := "light",
  micrositePalette := Map(
    "brand-primary" -> "#E05236",
    "brand-secondary" -> "#3F3242",
    "brand-tertiary" -> "#2D232F",
    "gray-dark" -> "#453E46",
    "gray" -> "#837F84",
    "gray-light" -> "#E3E2E3",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  micrositeGithubToken := sys.env.get( "GITHUB_TOKEN" ),
  micrositeGitterChannel := false
)

ThisBuild / GitKeys.gitReader := baseDirectory(base => new DefaultReadableGit( base ) ).value

lazy val slackMorphismMicrosite = project
  .in( file( "site" ) )
  .enablePlugins( MicrositesPlugin )
  .settings(
    name := "slack-morphism-microsite"
  )
  .settings( docSettings )
  .settings( noPublishSettings )
  .settings( docSettings )
  .dependsOn( slackMorphismModels, slackMorphismClient, slackMorphismExamples )