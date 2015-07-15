name := "indentation-lexical"

version := "0.5"

scalaVersion := "2.11.6"

scalacOptions ++= Seq( "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

incOptions := incOptions.value.withNameHashing(true)

organization := "ca.hyperreal"

//resolvers += Resolver.sonatypeRepo( "snapshots" )

resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies ++= Seq(
	"org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
	)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"

libraryDependencies ++= Seq(
	"ca.hyperreal" %% "lia" % "0.15"
	)


seq(bintraySettings:_*)

publishMavenStyle := true

//publishTo := Some( Resolver.sftp( "private", "hyperreal.ca", "/var/www/hyperreal.ca/maven2" ) )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/FunL/indentation-lexical"))

pomExtra := (
  <scm>
    <url>git@github.com:FunL/indentation-lexical.git</url>
    <connection>scm:git:git@github.com:FunL/indentation-lexical.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>http://funl-lang.org</url>
    </developer>
  </developers>)
