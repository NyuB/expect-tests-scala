import mill._, scalalib._

object Versions {
    val scala = "3.4.1"
    val munit = "1.0.0"
}

trait SharedConfiguration extends ScalaModule {
    override def scalaVersion: T[String] = Versions.scala
    override def scalacOptions: T[Seq[String]] =
        Seq(
          "-deprecation",
          "-Werror",
          "-Wimplausible-patterns",
          "-Wnonunit-statement",
          "-WunstableInlineAccessors",
          "-Wunused:all",
          "-Wvalue-discard",
          "-Xlint:all"
        )

    trait Tests extends ScalaTests with TestModule.Munit {
        override def ivyDeps = super.ivyDeps() ++ Agg(
          ivy"org.scalameta::munit:${Versions.munit}"
        )

    }

}

object expect extends ScalaModule with SharedConfiguration {
    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.scalameta::munit:${Versions.munit}"
    )

    object test extends Tests
}
