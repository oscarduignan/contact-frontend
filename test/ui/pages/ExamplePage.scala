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

package ui.pages

import org.openqa.selenium.WebDriver
import ui.conf.TestConfiguration

object ExamplePage extends BasePage {

  val url: String = "http://localhost:9250"
  val title       = "Authority Wizard"

  def login(redirectionUrl: String)(implicit driver: WebDriver): Unit = {
    textField("redirectionUrl").value = redirectionUrl
    singleSel("confidenceLevel").value = "50"
    singleSel("affinityGroup").value = "Individual"
    textField("nino").value = "MA000003A"
    click on CssSelectorQuery(".button")
  }

}
