package features

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import support.StubbedFeatureSpec
import support.page.SurveyPage._
import support.page.{SurveyConfirmationPageWelsh, SurveyPageWithTicketAndServiceIds, UnauthenticatedFeedbackPage}
import play.api.inject.guice.GuiceApplicationBuilder

@RunWith(classOf[JUnitRunner])
class LanguageSwitchingFeature extends StubbedFeatureSpec {

  override def useJavascript: Boolean = true

  override lazy val app = new GuiceApplicationBuilder()
    .configure(
      "application.langs" -> "en,cy",
      "enableLanguageSwitching" -> true)
    .build

  feature("Language Switching") {
//    MoveToAcceptanceTest
    scenario("Switch from English to Welsh in the survey") {

      Given("I go to the survey form page")
      goOn(new SurveyPageWithTicketAndServiceIds("HMRC-Z2V6DUK5", "arbitrary%20service%20id"))

      And("I click on the switch language link")
      click on linkText("Cymraeg")

      i_see("Arolwg")
      i_see("Pa mor fodlon ydych gyda'r ateb a gawsoch gennym?")
      i_see("Pa mor fodlon ydych gyda'r amser a gymerwyd gennym i'ch ateb?")
      i_see("Rhowch wybod i ni sut y gallwn wella'r cymorth a roddwn i chi.")
      i_see("2500 o gymeriadau neu lai")
      i_see("A oes unrhyw beth o'i le gyda'r dudalen hon?")
      i_see("English")

      And("I click on the switch language link")
      click on linkText("English")

      i_see("Survey")
      i_see("How satisfied are you with the answer we gave you?")
      i_see("How satisfied are you with the speed of our reply?")
      i_see("Tell us how we can improve the support we give you.")
      i_see("2500 characters or less")
      i_see("Is there anything wrong with this page?")
      i_see("Cymraeg")
    }

//    MoveToAcceptanceTest
    scenario("Show confirmation message in Welsh after submitting the form in Welsh") {
      val invalidTicketId = "HMRC-Z2V6DUK5"

      Given("I go to the survey form page")
      goOn(new SurveyPageWithTicketAndServiceIds(invalidTicketId,"arbitrary%20service%20id"))

      And("I click on the switch language link")
      click on linkText("Cymraeg")
      i_see("Arolwg")

      When("I successfully fill in the form")
      selectHowHelpfulTheResponseWas("strongly-agree")
      selectHowSpeedyTheResponseWas("strongly-agree")
      setAdditionalComment("Rhoedd eich mam yn fochdew a'ch tad yn aroglu o eirin ysgaw!")

      And("Submit the form")
      clickSubmitButton()

      And("I get to the Welsh Language confirmation page")
      on(SurveyConfirmationPageWelsh)
    }

//    MoveToAcceptanceTest
    scenario("Show beta-feedback-unauthenticated page ratings in Welsh") {
      Given("I go to the beta-feedback-unauthenticated page")
      goOn(UnauthenticatedFeedbackPage)

      When("I click on the switch language link")
      click on linkText("Cymraeg")

      Then("The ratings text is in Welsh")
      i_see("Anfon eich adborth")
      UnauthenticatedFeedbackPage.ratingsList() mustBe List("Da iawn", "Da", "Niwtral", "Gwael", "Gwael iawn")
    }
  }
}

