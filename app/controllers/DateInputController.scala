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
import play.api.data.validation.Constraints.pattern
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{BirthdayInputPage, DateInputConfirmationPage, DateInputPage}

import java.time.LocalDate
import javax.inject.Inject
import scala.util.{Failure, Success, Try}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

class DateInputController @Inject() (
  mcc: MessagesControllerComponents,
  dateInputPage: DateInputPage,
  birthdayInputPage: BirthdayInputPage,
  dateInputConfirmationPage: DateInputConfirmationPage
)(implicit
  appConfig: AppConfig
) extends FrontendController(mcc) {

  def index(): Action[AnyContent] = Action { implicit request =>
    val submitRoute = routes.DateInputController.submit()
    val form        = DateFormBinder.emptyForm
    Ok(dateInputPage(form, submitRoute))
  }

  def birthday(): Action[AnyContent] = Action { implicit request =>
    val submitRoute = routes.DateInputController.submit()
    val form        = DateFormBinder.emptyForm
    Ok(birthdayInputPage(form, submitRoute))
  }

  def submit(): Action[AnyContent] = Action { implicit request =>
    DateFormBinder.form
      .bindFromRequest()
      .fold(
        formWithError => BadRequest(dateInputPage(formWithError, routes.DateInputController.submit())),
        _ => Redirect(routes.DateInputController.thanks())
      )
  }

  def thanks(): Action[AnyContent] = Action { implicit request =>
    Ok(dateInputConfirmationPage())
  }
}

case class DateData(day: String, month: String, year: String)
case class DateTestData(date: DateData)

object DateFormBinder {

  def emptyForm: Form[DateTestData] = DateFormBinder.form.fill(
    DateTestData(DateData("", "", ""))
  )

  val dayConstraint: Constraint[String]   = pattern("[0-9]{1,2}".r, error = "dateinput.day.required")
  val monthConstraint: Constraint[String] = pattern("[0-9]{1,2}".r, error = "dateinput.month.required")
  val yearConstraint: Constraint[String]  = pattern("[0-9]{1,4}".r, error = "dateinput.year.required")

  val dateConstraint: Constraint[DateData] = Constraint("constraints.date") { dateData =>
    Try(toDate(dateData)) match {
      case Success(date) =>
        if (date.isBefore(LocalDate.now())) Valid else Invalid(Seq(ValidationError("dateinput.invalid.future")))
      case Failure(_)    => Invalid(Seq(ValidationError("dateinput.invalid")))
    }
  }

  private def toDate(date: DateData) = LocalDate.of(date.year.toInt, date.month.toInt, date.day.toInt)

  def form: Form[DateTestData] = Form[DateTestData](
    mapping(
      "date" -> mapping(
        "day"   -> text.verifying(dayConstraint),
        "month" -> text.verifying(monthConstraint),
        "year"  -> text.verifying(yearConstraint)
      )(DateData.apply)(DateData.unapply).verifying(dateConstraint)
    )(DateTestData.apply)(DateTestData.unapply)
  )
}
