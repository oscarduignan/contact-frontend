import sbt._

object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "contact-frontend"
  val appVersion = envOrElse("CONTACT_FRONTEND_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "frontend-bootstrap" % "4.4.0",
    "uk.gov.hmrc" %% "play-authorised-frontend" % "4.0.0",
    "uk.gov.hmrc" %% "play-config" % "2.0.1",
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.0",
    "uk.gov.hmrc" %% "play-health" % "1.1.0",
    "uk.gov.hmrc" %% "play-ui" % "4.2.0",
    "uk.gov.hmrc" %% "govuk-template" % "4.0.0",
    "uk.gov.hmrc" %% "url-builder" % "1.0.0",
    "org.apache.httpcomponents" % "httpclient" % "4.3.1"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "org.scalatestplus" %% "play" % "1.2.0" % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test",
    "org.jsoup" % "jsoup" % "1.7.3" % "test",
    "com.github.tomakehurst" % "wiremock" % "1.48" % "test",
    "uk.gov.hmrc" %% "scala-webdriver" % "4.27.0" % "test",
    "uk.gov.hmrc" %% "hmrctest" % "1.4.0" % "test"
  )

  def apply() = compile ++ test
}


