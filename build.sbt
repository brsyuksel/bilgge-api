lazy val dependencies = new {
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.1"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.3.1"
  lazy val commonsCodec = "commons-codec" % "commons-codec" % "1.15"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.13.0"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.13.0"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.13.0"
  lazy val jwtCore = "com.pauldijou" %% "jwt-core" % "4.2.0"
  lazy val jwtCirce = "com.pauldijou" %% "jwt-circe" % "4.2.0"
  lazy val postgresql = "org.postgresql" % "postgresql" % "42.2.18"
  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % "0.9.4"
  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % "0.9.4"
  lazy val doobiePG = "org.tpolecat" %% "doobie-postgres" % "0.9.4"
  lazy val doobiePGCirce = "org.tpolecat" %% "doobie-postgres-circe" % "0.9.4"
  lazy val circeConf = "io.circe" %% "circe-config" % "0.8.0"
  lazy val http4sDSL = "org.http4s" %% "http4s-dsl" % "0.21.14"
  lazy val http4sServer = "org.http4s" %% "http4s-blaze-server" % "0.21.14"
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % "0.21.14"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0"
  lazy val catsEffectTesting = "com.codecommit" %% "cats-effect-testing-scalatest" % "0.5.0"
}

lazy val root = (project in file("."))
  .settings(
    name := "bilgge",
    version := "0.1.0",
    scalaVersion := "2.13.4",
    scalacOptions ++= Seq(
      // thanks are going to: https://gist.github.com/tabdulradi/aa7450921756cd22db6d278100b2dac8
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      //  "-language:experimental.macros",   // Allow macro definition (besides implementation and application). Disabled, as this will significantly change in Scala 3
      "-language:higherKinds", // Allow higher-kinded types
      //  "-language:implicitConversions",   // Allow definition of implicit functions called views. Disabled, as it might be dropped in Scala 3. Instead use extension methods (implemented as implicit class Wrapper(val inner: Foo) extends AnyVal {}
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      // "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Xlint:unused", // TODO check if we still need -Wunused below
      "-Xlint:nonlocal-return", // A return statement used an exception for flow control.
      "-Xlint:implicit-not-found", // Check @implicitNotFound and @implicitAmbiguous messages.
      "-Xlint:serial", // @SerialVersionUID on traits and non-serializable classes.
      "-Xlint:valpattern", // Enable pattern checks in val definitions.
      "-Xlint:eta-zero", // Warn on eta-expansion (rather than auto-application) of zero-ary method.
      "-Xlint:eta-sam", // Warn on eta-expansion to meet a Java-defined functional interface that is not explicitly annotated with @FunctionalInterface.
      "-Xlint:deprecation", // Enable linted deprecations.
      "-Wdead-code", // Warn when dead code is identified.
      "-Wextra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Wmacros:both", // Lints code before and after applying a macro
      "-Wnumeric-widen", // Warn when numerics are widened.
      "-Woctal-literal", // Warn on obsolete octal syntax.
      "-Xlint:implicit-recursion", // Warn when an implicit resolves to an enclosing self-definition.
      "-Wunused:imports", // Warn if an import selector is not referenced.
      "-Wunused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Wunused:privates", // Warn if a private member is unused.
      "-Wunused:locals", // Warn if a local definition is unused.
      "-Wunused:explicits", // Warn if an explicit parameter is unused.
      "-Wunused:implicits", // Warn if an implicit parameter is unused.
      "-Wunused:params", // Enable -Wunused:explicits,implicits.
      "-Wunused:linted",
      "-Wvalue-discard", // Warn when non-Unit expression results are unused.
      "-Ybackend-parallelism",
      "12", // Enable paralellisation — change to desired number!
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified" // and macro definitions. This can lead to performance improvements.
    ),
    libraryDependencies ++= Seq(
      dependencies.catsCore,
      dependencies.catsEffect,
      dependencies.commonsCodec,
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeParser,
      dependencies.jwtCore,
      dependencies.jwtCirce,
      dependencies.postgresql,
      dependencies.doobieCore,
      dependencies.doobieHikari,
      dependencies.doobiePG,
      dependencies.doobiePGCirce,
      dependencies.circeConf,
      dependencies.http4sDSL,
      dependencies.http4sServer,
      dependencies.http4sCirce,
      dependencies.scalaTest % Test,
      dependencies.catsEffectTesting % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
