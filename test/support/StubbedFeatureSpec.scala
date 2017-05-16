package support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.openqa.selenium.WebDriver
import org.scalatest._
import org.scalatestplus.play.OneServerPerSuite
import support.behaviour.NavigationSugar
import support.steps.{ApiSteps, NavigationSteps, ObservationSteps}
import support.stubs._

trait StubbedFeatureSpec
  extends FeatureSpec
  with GivenWhenThen
  with Matchers
  with OneServerPerSuite
  with Stubs
  with BeforeAndAfter
  with BeforeAndAfterEach
  with org.scalatest.Background
  with BeforeAndAfterAll
  with NavigationSugar
  with NavigationSteps
  with ApiSteps
  with ObservationSteps
  with OptionValues {

  val testUsingWebDriver: WebDriver

  override lazy val port = 9000

  val stubPort = 11111
  val stubHost = "localhost"
  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  override def beforeAll(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    testUsingWebDriver.manage().deleteAllCookies()
    WireMock.reset()
    stubFor(Auditing)
    stubFor(Login)
    stubFor(Deskpro)
    stubFor(ExternalPages)
  }
}
