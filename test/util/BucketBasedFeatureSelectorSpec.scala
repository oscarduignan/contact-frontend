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

package util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.Request
import play.api.test.FakeRequest
import util.BucketCalculator.BucketCalculator

class BucketBasedFeatureSelectorSpec extends AnyWordSpec with Matchers {

  val testFeature1 = GetHelpWithThisPageFeatureFieldHints
  val testFeature2 = GetHelpWithThisPageImprovedFieldValidation
  val testFeature3 = GetHelpWithThisPageMoreVerboseConfirmation

  val mockBucketCalculator: BucketCalculator = request => request.headers.get("bucket").map(_.toInt).getOrElse(0)

  val testInstance = new BucketBasedFeatureSelector(
    mockBucketCalculator,
    Set(
      FeatureEnablingRule(0, 20, None, testFeature1),
      FeatureEnablingRule(10, 30, None, testFeature2),
      FeatureEnablingRule(10, 30, Some(Set("service1", "service2")), testFeature3)
    )
  )

  val service = Some("testService")

  "RequestBasedFeatureSelector" should {

    "return features Test1 and Test2 for user in bucket 10" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "10"))
      testInstance.computeFeatures(request, service) shouldBe Set(testFeature1, testFeature2)
    }

    "return feature Test1 for user in bucket 9" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "9"))
      testInstance.computeFeatures(request, service) shouldBe Set(testFeature1)
    }

    "return feature Test2 for user in bucket 20" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "20"))
      testInstance.computeFeatures(request, service) shouldBe Set(testFeature2)
    }

    "return no features for user in bucket 30" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "30"))
      testInstance.computeFeatures(request, service) shouldBe Set.empty[Feature]
    }

  }

  "ResquestBasedFeatureSelector with service provided" should {
    "enable feature if the service is on the whitelist" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "5"))
      testInstance.computeFeatures(request, Some("service1")) shouldBe Set(testFeature1)
    }

    "not enable feature if the service is not on the whitelist" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "5"))
      testInstance.computeFeatures(request, Some("other-service")) shouldBe Set(testFeature1)
    }

    "not enable feature if the service is not enabled" in {
      val request: Request[AnyRef] = FakeRequest().withHeaders(("bucket", "5"))
      testInstance.computeFeatures(request, None) shouldBe Set(testFeature1)
    }

  }

}
