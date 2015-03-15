package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data.format.Formats._
import anorm._
import models._
import play.api.libs.ws.WS

object Application extends Controller {

  val registrationForm: Form[TournamentRegistration] = Form(
  	mapping(
      "id"              -> ignored(None:Option[Long]),
      "sectionEntered"  -> nonEmptyText,
      "fee"             -> of[Double],
      "paymentConfirmed"-> ignored(false),
      "byeInRounds"     -> optional(text),

      "player"          -> mapping(
    	"id"              -> ignored(None:Option[Long]),
        "firstName"       -> nonEmptyText,
        "lastName"        -> nonEmptyText,
        "email"           -> email,
        "address"         -> nonEmptyText,
  	    "postcode"        -> nonEmptyText,
  	    "phone"           -> nonEmptyText,
  	    "dateOfBirth"     -> optional(date("dd/MM/yyyy")),
  	    "chessclub"       -> optional(text),
  	    "ecfMembershipNo" -> optional(text),
  	    "ecfGradingRef"   -> optional(text),
  	    "rapidGrade"      -> optional(number),
  	    "slowGrade"       -> optional(number),
        "gradeEstimate"   -> optional(text)
      )(ChessPlayer.apply)(ChessPlayer.unapply)
    )(TournamentRegistration.apply)(TournamentRegistration.unapply)
  )

  /**
   * Display an empty form.
   */
  def form = Action {
    Ok(views.html.index(registrationForm));
  }

  
  /**
   * Handle form submission.
   */
  def submit = Action { implicit request =>
    registrationForm.bindFromRequest.fold(
      errors => {
        Logger.info(s"Received bad submission request: $errors")
        BadRequest(views.html.index(errors))
      },
      registration =>  {
        Logger.info(s"Received registration: $registration")
      	val id = TournamentRegistration.insert(registration).get
      	Logger.info(s"Persisted registration of: ${registration.player.lastName}")
      	Ok(views.html.registrationSuccess(registration, id))
      }
    )
  }
  
   /**
   * Display registration list.
   */
  def list = Action { implicit request =>
    val notifications = PaypalNotification.list().groupBy(pn => pn.registrationId)
    val regs = TournamentRegistration.list()
    val regTuples = regs.map(r => {
      val id = r.id.get.toInt
      (r, notifications.getOrElse(id, Seq())) 
    })
    val existingRegs = regs.map(r => r.id.get.toInt)
    val existingRegsSet = Set(existingRegs: _*) 
    val unassigned = notifications.map(n => if (existingRegsSet.contains(n._1)) None else Some(n._2)).
    	filter(_.isDefined).flatMap(_.get)
    	Ok(views.html.list(regTuples, unassigned))
  }

}