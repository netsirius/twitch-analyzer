lazy val root = (project in file("."))
  .settings(
    name := "twitch-analyzer",
    organization := "com.twitchanalyzer",
    ThisBuild / version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.4",
    isSnapshot := version.value.endsWith("SNAPSHOT"),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "src/main/resources"
  )

lazy val sparkVersion = "3.2.0"

resolvers ++= Seq(Resolver.mavenCentral, Resolver.mavenLocal)

lazy val deps = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "com.typesafe" % "config" % "1.3.3",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "com.softwaremill.sttp" %% "core" % "1.7.2",
  "com.softwaremill.sttp.client3" %% "core" % "3.2.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.4",
  "com.github.apanimesh061" % "vader-sentiment-analyzer" % "1.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "8.11.2"
)

libraryDependencies ++= deps

assembly / assemblyOption := (assembly / assemblyOption).value
  .copy(includeScala = false)

assembly / assemblyMergeStrategy := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*)
      if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", _ @_*) =>
    MergeStrategy.discard
  case _ =>
    MergeStrategy.first
}

assembly / assemblyJarName := s"${name.value}.jar"
assembly / test := {}
