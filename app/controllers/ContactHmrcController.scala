/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package controllers

import config.AppConfig
import connectors.deskpro.HmrcDeskproConnector
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.filters.csrf.CSRF
import services.{CaptchaService, DeskproSubmission}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import util.DeskproEmailValidator
import views.html.helpers.recaptcha
import views.html.partials.{contact_hmrc_form, contact_hmrc_form_confirmation}
import views.html.{DeskproErrorPage, contact_hmrc, contact_hmrc_confirmation}

import scala.concurrent.{ExecutionContext, Future}

object ContactHmrcForm {

  private val emailValidator                     = new DeskproEmailValidator()
  private val validateEmail: (String) => Boolean = emailValidator.validate

  val form = Form[ContactForm](
    mapping(
      "contact-name"     -> text
        .verifying("error.common.problem_report.name_mandatory", name => !name.trim.isEmpty)
        .verifying("error.common.problem_report.name_too_long", name => name.size <= 70),
      "contact-email"    -> text
        .verifying("error.common.problem_report.email_valid", validateEmail)
        .verifying("deskpro.email_too_long", email => email.size <= 255),
      "contact-comments" -> text
        .verifying("error.common.comments_mandatory", comment => !comment.trim.isEmpty)
        .verifying("error.common.comments_too_long", comment => comment.size <= 2000),
      "isJavascript"     -> boolean,
      "referrer"         -> text,
      "csrfToken"        -> text,
      "service"          -> optional(text),
      "abFeatures"       -> optional(text),
      "userAction"       -> optional(text)
    )(ContactForm.apply)(ContactForm.unapply)
  )
}

@Singleton
class ContactHmrcController @Inject() (
  val hmrcDeskproConnector: HmrcDeskproConnector,
  val authConnector: AuthConnector,
  val captchaService: CaptchaService,
  val configuration: Configuration,
  mcc: MessagesControllerComponents,
  contactPage: contact_hmrc,
  contactConfirmationPage: contact_hmrc_confirmation,
  contactHmrcForm: contact_hmrc_form,
  contactHmrcFormConfirmation: contact_hmrc_form_confirmation,
  deskproErrorPage: DeskproErrorPage,
  recaptcha: recaptcha
)(implicit val appConfig: AppConfig, val executionContext: ExecutionContext)
    extends WithCaptcha(mcc, deskproErrorPage, recaptcha)
    with DeskproSubmission
    with AuthorisedFunctions
    with LoginRedirection
    with I18nSupport
    with ContactFrontendActions {

  def index = Action.async { implicit request =>
    loginRedirection(routes.ContactHmrcController.index().url)(authorised(AuthProviders(GovernmentGateway)) {
      Future.successful {
        val referrer  = request.headers.get(REFERER).getOrElse("n/a")
        val csrfToken = CSRF.getToken(request).map(_.value).getOrElse("")
        Ok(contactPage(ContactHmrcForm.form.fill(ContactForm(referrer, csrfToken, None, None, None)), loggedIn = true))
      }
    })
  }

  def indexUnauthenticated(service: String, userAction: Option[String], referrerUrl: Option[String]) = Action.async {
    implicit request =>
      Future.successful {
        val httpReferrer = request.headers.get(REFERER)
        val referrer     = referrerUrl orElse httpReferrer getOrElse "n/a"
        val csrfToken    = CSRF.getToken(request).map(_.value).getOrElse("")
        Ok(
          contactPage(
            ContactHmrcForm.form.fill(ContactForm(referrer, csrfToken, Some(service), None, userAction)),
            loggedIn = false,
            reCaptchaComponent = Some(recaptchaFormComponent("contact-hmrc"))
          )
        )
      }
  }

  def submit = Action.async { implicit request =>
    loginRedirection(routes.ContactHmrcController.index().url)(
      authorised(AuthProviders(GovernmentGateway)).retrieve(Retrievals.allEnrolments) { allEnrolments =>
        validateCaptcha(request) {
          handleSubmit(Some(allEnrolments), routes.ContactHmrcController.thanks())
        }
      }
    )
  }

  def submitUnauthenticated = Action.async { implicit request =>
    validateCaptcha(request) {
      handleSubmit(None, routes.ContactHmrcController.thanksUnauthenticated())
    }
  }

  private def handleSubmit(enrolments: Option[Enrolments], thanksRoute: Call)(implicit request: Request[AnyContent]) =
    ContactHmrcForm.form
      .bindFromRequest()(request)
      .fold(
        error => Future.successful(BadRequest(contactPage(error, enrolments.isDefined))),
        data =>
          createDeskproTicket(data, enrolments)
            .map { ticketId =>
              Redirect(thanksRoute).withSession(request.session + ("ticketId" -> ticketId.ticket_id.toString))
            }
            .recover { case _ =>
              InternalServerError(deskproErrorPage())
            }
      )

  def thanks = Action.async { implicit request =>
    loginRedirection(routes.ContactHmrcController.index().url)(authorised(AuthProviders(GovernmentGateway)) {
      doThanks(request)
    })
  }

  def thanksUnauthenticated = Action.async { implicit request =>
    doThanks(request)
  }

  private def doThanks(implicit request: Request[AnyRef]) = {
    val result = request.session.get("ticketId").fold(BadRequest("Invalid data")) { ticketId =>
      Ok(contactConfirmationPage(ticketId))
    }
    Future.successful(result)
  }

  def contactHmrcPartialForm(submitUrl: String, csrfToken: String, service: Option[String], renderFormOnly: Boolean) =
    Action.async { implicit request =>
      Future.successful {
        Ok(
          contactHmrcForm(
            ContactHmrcForm.form.fill(
              ContactForm(request.headers.get(REFERER).getOrElse("n/a"), csrfToken, service, None, None)
            ),
            submitUrl,
            renderFormOnly
          )
        )
      }
    }

  def submitContactHmrcPartialForm(resubmitUrl: String, renderFormOnly: Boolean) = Action.async { implicit request =>
    ContactHmrcForm.form
      .bindFromRequest()(request)
      .fold(
        error => Future.successful(BadRequest(contactHmrcForm(error, resubmitUrl, renderFormOnly))),
        data =>
          (for {
            enrolments <- maybeAuthenticatedUserEnrolments()
            ticketId   <- createDeskproTicket(data, enrolments)
          } yield Ok(ticketId.ticket_id.toString)).recover { case _ =>
            InternalServerError(deskproErrorPage())
          }
      )
  }

  def contactHmrcPartialFormConfirmation(ticketId: String) = Action { implicit request =>
    Ok(contactHmrcFormConfirmation(ticketId))
  }
}

case class ContactForm(
  contactName: String,
  contactEmail: String,
  contactComments: String,
  isJavascript: Boolean,
  referrer: String,
  csrfToken: String,
  service: Option[String] = Some("unknown"),
  abFeatures: Option[String] = None,
  userAction: Option[String] = None
)

object ContactForm {
  def apply(
    referrer: String,
    csrfToken: String,
    service: Option[String],
    abFeatures: Option[String],
    userAction: Option[String]
  ): ContactForm =
    ContactForm("", "", "", isJavascript = false, referrer, csrfToken, service, abFeatures, userAction)
}
