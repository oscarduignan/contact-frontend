import sbt._

object AppDependencies {

  def dependencies(testPhases: Seq[String]): Seq[ModuleID] = {
    val testDependencies = testPhases.flatMap(test)
    compile ++ testDependencies
  }

  private val compile = Seq(
    "uk.gov.hmrc"              %% "bootstrap-frontend-play-28" % "5.3.0",
    "uk.gov.hmrc"              %% "govuk-template"             % "5.65.0-play-27",
    "uk.gov.hmrc"              %% "play-ui"                    % "9.4.0-play-28",
    "uk.gov.hmrc"              %% "url-builder"                % "3.5.0-play-28",
    "uk.gov.hmrc"              %% "auth-client"                % "5.6.0-play-28",
    "commons-validator"         % "commons-validator"          % "1.6",
    "org.apache.httpcomponents" % "httpclient"                 % "4.4.1",
    "uk.gov.hmrc"              %% "play-frontend-hmrc"         % "0.66.0-play-28"
  )

  private def test(scope: String) = Seq(
    "org.scalatest"          %% "scalatest"                % "3.1.1"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "4.0.0"          % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito"    % "1.0.0-M2"       % scope,
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % scope,
    "com.typesafe.play"      %% "play-test"                % "2.7.4"          % scope,
    "org.mockito"             % "mockito-all"              % "2.0.2-beta"     % scope,
    "org.jsoup"               % "jsoup"                    % "1.11.3"         % scope,
    "com.github.tomakehurst"  % "wiremock"                 % "1.58"           % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.14.0"         % scope,
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % "5.3.0"          % scope,
    "uk.gov.hmrc"            %% "domain"                   % "5.11.0-play-27" % scope,
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.35.10"        % scope,
    "uk.gov.hmrc"            %% "webdriver-factory"        % "0.18.0"         % scope,
    "org.scalatestplus"      %% "selenium-3-141"           % "3.2.0.0"        % scope,
    "com.github.tomakehurst"  % "wiremock"                 % "2.18.0"         % "test"
  )
}
