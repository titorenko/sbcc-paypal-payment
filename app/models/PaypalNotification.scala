package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class PaypalNotification (
  id: Option[Long] = None,
  registrationId: Int,
  receiverEmail: String,
  paymentAmount: String,
  paymentCcy: String,
  paymentStatus: String,
  verificationStatus: String,
  firstName: String,
  lastName: String,
  fullNotification: String
)


object PaypalNotification {
  def from(request: Map[String,Seq[String]], verStatus: String): PaypalNotification = {
    PaypalNotification(
        None,
        parseId(request("custom")),
        request("receiver_email").mkString,
        request("mc_gross").mkString,
        request("mc_currency").mkString,
        request("payment_status").mkString,
        verStatus,
        request("first_name").mkString,
        request("last_name").mkString,
        request.toString
    )
  }
  
   /**
   * Parse a PaypalNotification from a ResultSet
   */
  val simple = {
    get[Option[Long]]("pp_notification.id") ~
    get[Int]("pp_notification.registration_id") ~
    get[String]("pp_notification.receiver_email") ~
    get[String]("pp_notification.payment_amount") ~
    get[String]("pp_notification.payment_ccy") ~
    get[String]("pp_notification.payment_status") ~
    get[String]("pp_notification.verification_status") ~
    get[String]("pp_notification.first_name") ~
    get[String]("pp_notification.last_name") ~
    get[String]("pp_notification.full_notification") map {
      case id~registrationId~receiverEmail~paymentAmount~paymentCcy~paymentStatus~verificationStatus~firstName~lastName~fullNotification => 
        PaypalNotification(id, registrationId, receiverEmail, paymentAmount, paymentCcy, paymentStatus, verificationStatus, firstName, lastName, fullNotification)
    }
  }
  
  private def parseId(input: Seq[String]): Int = {
    try {
      Integer.parseInt(input.mkString)
    } catch {
      case _: Throwable => -1
    }
  }
  
  def insert(ppn: PaypalNotification): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
    	insert into pp_notification (
    	  registration_id, receiver_email, payment_amount, payment_ccy, payment_status,
          verification_status, first_name, last_name, full_notification 
    	) values (	         
		  {registration_id}, {receiver_email}, {payment_amount}, {payment_ccy}, {payment_status},
          {verification_status}, {first_name}, {last_name}, {full_notification}
        )""").on(
          'registration_id -> ppn.registrationId ,
          'receiver_email -> ppn.receiverEmail,
          'payment_amount -> ppn.paymentAmount,
          'payment_ccy -> ppn.paymentCcy ,
          'payment_status -> ppn.paymentStatus,
          'verification_status -> ppn.verificationStatus,
          'first_name -> ppn.firstName,
          'last_name -> ppn.lastName,
          'full_notification -> shorten(ppn.fullNotification, 4095)
       ).executeInsert()
    }
  }
  
  def list(): Seq[PaypalNotification] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from pp_notification
        """
      ).as(PaypalNotification.simple *)
    }
  }

  private def shorten(str: String, maxLength: Int): String = {
    if (str.length < maxLength) str else str.substring(maxLength)
  }
}