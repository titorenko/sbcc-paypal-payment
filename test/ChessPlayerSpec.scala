import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
@RunWith(classOf[JUnitRunner])
class ModelSpec extends Specification {
  import models._

  "Tournament Registration model" should {
    "be saved" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val player = ChessPlayer(None, "Angus", "French", "french@angus.com", "Streatham", "SW16", "123", None, None, None, None, None, None, None)
        TournamentRegistration.insert(TournamentRegistration(None, "Open", 20, false, None, player))
        val regs = TournamentRegistration.list
        regs.size must equalTo(1)
        regs(0).player.firstName  must equalTo(player.firstName)
        regs(0).player.lastName  must equalTo(player.lastName)
        regs(0).player.email  must equalTo(player.email)
      }
    }
  }
}