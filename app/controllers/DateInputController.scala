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

package controllers

import config.AppConfig
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{DateInputConfirmationPage, DateInputPage}

import javax.inject.Inject

class DateInputController @Inject() (mcc: MessagesControllerComponents, dateInputPage: DateInputPage, dateInputConfirmationPage: DateInputConfirmationPage)(implicit
  appConfig: AppConfig
) extends FrontendController(mcc) {

  def index(): Action[AnyContent] = Action { implicit request =>
    val submitRoute = routes.DateInputController.submit()
    val form = DateFormBinder.emptyForm
    Ok(dateInputPage(form, submitRoute))
  }

  def submit() = Action { implicit request =>
    DateFormBinder.form
      .bindFromRequest()
      .fold(
        formWithError => BadRequest(dateInputPage(formWithError, routes.DateInputController.submit())),
        _ => Redirect(routes.DateInputController.thanks())
      )
  }

  def thanks() = Action { implicit request =>
    Ok(dateInputConfirmationPage())
  }
}

case class DateForm(day: Option[Int], month: Option[Int], year: Option[Int])

object DateFormBinder {

  def emptyForm: Form[DateForm] = DateFormBinder.form.fill(
    DateForm(None, None, None)
  )

  def form: Form[DateForm] = Form[DateForm](
    mapping(
      "date.day"   -> optional(number).verifying(_.nonEmpty),
      "date.month" -> optional(number).verifying(_.nonEmpty),
      "date.year"  -> optional(number).verifying(_.nonEmpty)
    )(DateForm.apply)(DateForm.unapply)
  )

}
