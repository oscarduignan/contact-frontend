/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ui.specs

import ui.pages.{ExamplePage, PayOnlinePage}
import ui.specs.BaseSpec
import ui.specs.tags.ZapTests

class ExampleSpec extends BaseSpec {

  info("Example Spec using ScalaTest")

  Feature("User accessing payments page") {

    Scenario("A logged in user is able to access payment details page") { //Remove ZapTests tag if not required

      Given("A logged in user accesses payments page")

      go to ExamplePage
      pageTitle shouldBe ExamplePage.title
      ExamplePage.login(PayOnlinePage.url)

      eventually {
        pageTitle shouldBe PayOnlinePage.title
      }

      When("User chooses to pay VAT tax")
      val vatId = "tax_to_pay-2"
      click on radioButton(vatId)
      click on "next"
      eventually {
        pageTitle shouldBe "Choose a way to pay - Pay your VAT - GOV.UK"
      }

      Then("Choose a way to pay page is displayed")
      tagName("h1").element.text shouldBe "Choose a way to pay"
    }
  }
}
