package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import play.api.mvc.BodyParsers.parse
import scala.collection.immutable.ListMap
import models.PaypalNotification
import models.TournamentRegistration

object Paypal extends Controller {
  
  val paypalUrl = current.configuration.getString("paypal.url").get
  val expectedReceiver = current.configuration.getString("paypal.business.email").get

  //1. PayPal HTTP POSTs your listener an IPN message that notifies you of an event.
  def ipn = Action { implicit request => 
    scala.concurrent.Future {
      processIPN(request.body)
    }
    Ok("")  //2. Your listener returns an empty HTTP 200 response to PayPal.
  }
  
  private[this] def processIPN(body: AnyContent) {
    Logger.info(s"Received paypal ipn with body ${body.asFormUrlEncoded}")
	
    //3.  Your listener HTTP POSTs the complete, unaltered message back to PayPal; 
    //the message must contain the same fields (in the same order) as the original 
    //message and be encoded in the same way as the original message.
    val request = body.asFormUrlEncoded.get
    val response = ListMap("cmd" -> Seq("_notify-validate")) ++ request
    Logger.info(s"Responding back with _notify-validate to $paypalUrl")
    WS.url(paypalUrl).post(response).map { response =>
	  saveNotification(request, response) //4. PayPal sends a single word back - either VERIFIED (if the message matches the original) or INVALID 
    }
  }
  
  private[this] def saveNotification(request: Map[String,Seq[String]], response: WSResponse) {
	try {
      Logger.info(s"Verification response status ${response.statusText} (${response.status}) with body ${response.body}")
      val ppStatus = response.body
      val pn = PaypalNotification.from(request, ppStatus)
      val id = PaypalNotification.insert(pn)
      Logger.info(s"PaypalNotification persisted with id=$id");
	} catch {
      case e: Exception => Logger.error(s"Failed to process request", e)
    }
  }
  
  /**
   * Verify that expected fee is paid in full 
   */
  def paimentStatus(reg: TournamentRegistration, notifications: Seq[PaypalNotification]): String = {
    val wrongEmail = notifications.map(n => n.receiverEmail).filterNot(e => e.equals(expectedReceiver))
    if (!wrongEmail.isEmpty) return "Spoof detected!\nWrong receiver: "+wrongEmail.distinct.mkString(", ")
    
    val invalids = notifications.filterNot(n => n.verificationStatus.equals("VERIFIED"))
    if (!invalids.isEmpty) return "Hack attempt detected!\nInvalid payments: "+invalids.mkString("\n")
    
    val totalPaid = notifications.filter(_.paymentStatus.equals("Completed")).
    	map(n => parsePaymentAmount(n.paymentAmount)).sum
    val diff = totalPaid - reg.fee 
    
    if (Math.abs(diff) < 0.01)  "Paid in full"
    else if (diff < 0) "Still to pay: "+(-diff)
    else "Overpaid by "+diff
  }
  
  private def parsePaymentAmount(amount: String): Double = {
    try {
      amount.toDouble
    } catch {
      case _: Exception => 0.0
    }
  }
}