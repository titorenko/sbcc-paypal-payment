package models

import java.util.{Date}
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.sql.Connection

case class TournamentRegistration (
  id: Option[Long] = None,
  sectionEntered: String,
  fee: Double,
  paymentConfirmed: Boolean,
  byeInRounds: Option[String],
  player: ChessPlayer
)

case class ChessPlayer (
  id: Option[Long] = None,
  firstName: String, 
  lastName: String,  
  email: String,
  address: String,
  postcode: String,
  phone: String,  
  dateOfBirth: Option[Date],
  
  chessclub: Option[String],
  ecfMembershipNo: Option[String],
  ecfGradingRef: Option[String],
  rapidGrade: Option[Int],
  slowGrade: Option[Int],
  gradeEstimate: Option[String]
)

object TournamentRegistration {
  /**
   * Parse a registration from a ResultSet
   */
  val simple = {
    get[Option[Long]]("registration.id") ~
    get[String]("registration.section") ~
    get[Double]("registration.fee") ~
    get[Boolean]("registration.payment_confirmed") ~
    get[Option[String]]("registration.bye_rounds") map {
      case id~section~fee~confirmed~byeRounds => (id, section, fee, confirmed, byeRounds)
    }
  }

  /**
   * Parse a (Registration,Player) from a ResultSet
   */
  val withPlayer = TournamentRegistration.simple ~ ChessPlayer.simple map {
    case t~player => TournamentRegistration(t._1, t._2, t._3, t._4, t._5, player)
  }

  def insert(registration: TournamentRegistration) = {
     DB.withTransaction {implicit connection =>
     	val playerId = ChessPlayer.insert(registration.player)
     	SQL(
     	  """
	        insert into registration(section, fee, payment_confirmed, bye_rounds, player_id)
     	    values (	         
	          {section}, {fee}, {payment_confirmed}, {bye_rounds}, {player_id}
     		)
     	  """
     	).on(
	      'section -> registration.sectionEntered ,
	      'fee -> registration.fee,
	      'payment_confirmed -> registration.paymentConfirmed,
	      'bye_rounds -> registration.byeInRounds,
	      'player_id -> playerId
     	).executeInsert()
     }
  }

  def list(): Seq[TournamentRegistration] = {
    
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from registration 
          left join chessplayer on registration.player_id = chessplayer.id
        """
      ).as(TournamentRegistration.withPlayer *)
    }
    
  }
}

object ChessPlayer {

  /**
   * Parse a ChessPlayer from a ResultSet
   */
  val simple = {
    get[Option[Long]]("chessplayer.id") ~
    get[String]("chessplayer.first_name") ~
    get[String]("chessplayer.last_name") ~
    get[String]("chessplayer.email") ~
    get[String]("chessplayer.address") ~
    get[String]("chessplayer.postcode") ~
    get[String]("chessplayer.phone") ~
    get[Option[Date]]("chessplayer.date_of_birth") ~
    get[Option[String]]("chessplayer.chessclub") ~
    get[Option[String]]("chessplayer.ecf_no") ~
    get[Option[String]]("chessplayer.ecf_grade_ref") ~
    get[Option[Int]]("chessplayer.rapid_grade") ~
    get[Option[Int]]("chessplayer.slow_grade") ~
    get[Option[String]]("chessplayer.grade_estimate") map {
      case id~firstName~lastName~email~address~postcode~phone~dateOfBirth~chessclub~ecfMembershipNo~ecfGradingRef~rapidGrade~slowGrade~gradeEstimate => 
        ChessPlayer(id, firstName, lastName, email, address, postcode, phone, dateOfBirth, chessclub, ecfMembershipNo, ecfGradingRef, rapidGrade, slowGrade, gradeEstimate)
    }
  }

  def insert(player: ChessPlayer)(implicit connection: Connection): Option[Long] = {
	  SQL(
	    """
	      insert 
	        into chessplayer (
	          first_name, last_name, email, address, postcode, phone,
			  date_of_birth, chessclub, ecf_no, ecf_grade_ref, rapid_grade, slow_grade, grade_estimate
	        )
			values (	         
			  {first_name}, {last_name}, {email}, {address}, {postcode}, {phone},
			  {date_of_birth}, {chessclub}, {ecf_no}, {ecf_grade_ref}, {rapid_grade}, 
        {slow_grade}, {grade_estimate}
			)
	    """
	  ).on(
	    'first_name -> player.firstName,
	    'last_name -> player.lastName,
	    'email -> player.email,
	    'address -> player.address,
	    'postcode -> player.postcode,
	    'phone -> player.phone,
	    'date_of_birth -> player.dateOfBirth,
	    'chessclub -> player.chessclub,
	    'ecf_no -> player.ecfMembershipNo,
	    'ecf_grade_ref -> player.ecfGradingRef,
	    'rapid_grade -> player.rapidGrade,
	    'slow_grade -> player.slowGrade, 
      'grade_estimate -> player.gradeEstimate
	  ).executeInsert()
  }
  
}