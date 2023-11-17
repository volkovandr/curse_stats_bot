import org.scalatest.BeforeAndAfterEach
import volkovandr.cursestatsbot.service.StatisticsService

class StatisticsServiceTest extends FlatSpecTestBase with BeforeAndAfterEach {
  var service = new StatisticsService()

  override def beforeEach(): Unit = {
    service.clear()
    service.stats.cursesPerChat.clear()
  }

  "The StatisticsService" should "add words" in {
    service.addWords(1, "user1", Seq("word1", "word2"))
    service.addWords(1, "user2", Seq("word1", "word3"))
    service.addWords(2, "user1", Seq("word1", "word2"))
    service.addWords(2, "user3", Seq("word1", "word3"))

    service.stats.cursesPerChatPerUser should have size(2)
    service.stats.cursesPerChatPerUser(1) should have size(2)
    service.stats.cursesPerChatPerUser(2) should have size(2)
    service.stats.cursesPerChatPerUser(1)("user1") should contain allOf("word1", "word2")
    service.stats.cursesPerChatPerUser(1)("user2") should contain allOf("word1", "word3")
    service.stats.cursesPerChatPerUser(2)("user1") should contain allOf("word1", "word2")
    service.stats.cursesPerChatPerUser(2)("user3") should contain allOf("word1", "word3")

    service.stats.cursesPerChat(1) should contain allOf("word1", "word2", "word3")
    service.stats.cursesPerChat(2) should contain allOf("word1", "word2", "word3")
  }

  it should "add words when there are some words already" in {
    service.addWords(1, "user1", Seq("word1", "word2"))
    service.addWords(1, "user1", Seq("word4", "word5"))

    service.stats.cursesPerChatPerUser(1)("user1") should contain allOf("word1", "word2", "word4", "word5")
    service.stats.cursesPerChat(1) should contain allOf("word1", "word2", "word4", "word5")
  }

  it should "find the user that cursed the most for a given chatId" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word1", "word3"))
    service.addWords(2, "user1", Seq("word1", "word2"))
    service.addWords(2, "user3", Seq("word1", "word3", "word4"))

    service.findMostCursingUsers(1) should contain only (("user1", 3))
    service.findMostCursingUsers(2) should contain only (("user3", 3))
  }

  it should "find multiple users that cursed most of all for a given chatId" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word1", "word3"))
    service.addWords(2, "user1", Seq("word1", "word2"))
    service.addWords(2, "user3", Seq("word1", "word3", "word4"))
    service.addWords(2, "user4", Seq("word1", "word3", "word4"))

    service.findMostCursingUsers(1) should contain only (("user1", 3))
    service.findMostCursingUsers(2) should contain only (("user3", 3), ("user4", 3))
  }

  it should "find the most used curses for a given chatId" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word1", "word4"))
    service.addWords(2, "user1", Seq("word1", "word2"))
    service.addWords(2, "user3", Seq("word1", "word3", "word4"))

    service.findMostUsedCurses(1) should contain only (("word1", 2))
    service.findMostUsedCurses(2) should contain only (("word1", 2))
  }

  it should "find multiple most used curses for a given chatId" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word1", "word4"))
    service.addWords(2, "user1", Seq("word1", "word2", "word3"))
    service.addWords(2, "user3", Seq("word1", "word3"))
    service.addWords(2, "user4", Seq("word1", "word3", "word4"))

    service.findMostUsedCurses(1) should contain only (("word1", 2))
    service.findMostUsedCurses(2) should contain only (("word1", 3), ("word3", 3))
  }

  it should "get total number of curses for a given chat" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word1", "word4"))
    service.addWords(2, "user1", Seq("word1", "word2", "word3"))
    service.addWords(2, "user3", Seq("word1", "word3"))
    service.addWords(2, "user4", Seq("word1", "word3", "word4"))

    service.getTotalNumberOfCurses(1) should be(5)
    service.getTotalNumberOfCurses(2) should be(8)
  }

  it should "not crash when asked for stats with not existing chatId" in {
    service.getTotalNumberOfCurses(3) should be(0)
    service.findMostUsedCurses(3) should be(empty)
    service.findMostCursingUsers(3) should be(empty)
  }

  it should "return the discoveryOfTheDay for a given chat" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word4", "word5"))
    service.addWords(2, "user3", Seq("word6", "word7", "word8"))
    service.addWords(2, "user4", Seq("word9", "word10"))

    service.getDiscoveryOfTheDay(1) should be(Some("user1", "word1"))
    service.getDiscoveryOfTheDay(2) should be(Some("user3", "word6"))
  }

  it should "return the discoveryOfTheDay after statistics cleared" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word4", "word5"))
    service.addWords(2, "user3", Seq("word6", "word7", "word8"))
    service.addWords(2, "user4", Seq("word9", "word10"))

    service.getDiscoveryOfTheDay(1) should be(Some("user1", "word1"))
    service.getDiscoveryOfTheDay(2) should be(Some("user3", "word6"))

    service.clear()

    service.getDiscoveryOfTheDay(1) should be (None)
    service.getDiscoveryOfTheDay(2) should be (None)

    service.addWords(1, "user1", Seq("word11"))

    service.getDiscoveryOfTheDay(1) should be (Some("user1", "word11"))
  }

  it should "ignore already existing words even after statistics cleared when returning the discovery of the day" in {
    service.addWords(1, "user1", Seq("word1", "word2", "word3"))
    service.addWords(1, "user2", Seq("word4", "word5"))
    service.addWords(2, "user3", Seq("word6", "word7", "word8"))
    service.addWords(2, "user4", Seq("word9", "word10"))

    service.getDiscoveryOfTheDay(1) should be(Some("user1", "word1"))
    service.getDiscoveryOfTheDay(2) should be(Some("user3", "word6"))

    service.clear()

    service.getDiscoveryOfTheDay(1) should be (None)
    service.getDiscoveryOfTheDay(2) should be (None)

    service.addWords(1, "user1", Seq("word1"))

    service.getDiscoveryOfTheDay(1) should be (None)
  }

}
