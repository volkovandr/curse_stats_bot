import volkovandr.cursestatsbot.configuration.BotConfiguration
import volkovandr.cursestatsbot.service.MessageService

class MessageServiceTest extends FlatSpecTestBase {
  val config = new BotConfiguration()
  val service = new MessageService(config)

  private val strings = Map("user" -> "Batman", "curse" -> "Damn")
  private val numerics = Map("cursesCount" -> 3, "usersCount" -> 2, "0" -> 0, "1" -> 1, "5" -> 5, "10" -> 10, "11" -> 11,
    "18" -> 18, "20" -> 20, "21" -> 21, "22" -> 22, "100" -> 100, "101" -> 101, "118" -> 118, "120" -> 120, "121" -> 121)
  private val lists = Map("curseList" -> Seq("word1", "word2", "word3"), "userList" -> Seq("user1", "user2"))

  private def replaceSingle(template: String): String = service.replaceSingle(template, strings, numerics, lists)

  "ReplaceSingle" should "replace string template" in {
    replaceSingle("user") shouldEqual "Batman"
  }

  it should "replace numeric template" in {
    replaceSingle("cursesCount") shouldEqual "3"
  }

  it should "replace list template" in {
    replaceSingle("curseList") shouldEqual "word1, word2, word3"
  }

  it should "replace list with separator" in {
    replaceSingle("curseList:and") shouldEqual "word1, word2 and word3"
  }

  it should "replace with options" in {
    replaceSingle("option1|option2|option3") should (equal("option1") or equal("option2") or equal("option3"))
  }

  it should "replace numerics with equal option" in {
    replaceSingle("cursesCount=>1:one|2:two|3:three|4:four") shouldEqual "three"
  }

  it should "replace numerics with range option" in {
    replaceSingle("cursesCount=>1-2:few|3-6:some|7-9:many") shouldEqual "some"
  }

  it should "replace numerics with more option" in {
    replaceSingle("cursesCount=>>10:many|>2:some|>0:few") shouldEqual "some"
  }

  it should "replace numerics with more or equals option" in {
    replaceSingle("cursesCount=>>=10:many|>=2:some|>=0:few") shouldEqual "some"
  }

  it should "replace numerics with less option" in {
    replaceSingle("cursesCount=><2:few|<5:some|>4:many") shouldEqual "some"
  }

  it should "replace numerics with less or equals option" in {
    replaceSingle("cursesCount=><2:few|<=5:some|>5:many") shouldEqual "some"
  }

  it should "replace numerics considering cyrillic equals option" in {
    replaceSingle("0=>cyr0:a|cyr1:b|cyr5:c") shouldEqual "a"
    replaceSingle("1=>cyr0:a|cyr1:b|cyr5:c") shouldEqual "b"
    replaceSingle("5=>cyr0:a|cyr1:b|cyr5:c") shouldEqual "c"
  }

  it should "replace numerics consitering cyrillic equals with numbers more than 10" in {
    replaceSingle("10=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("11=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("18=>cyr0:a|cyr1:b|cyr8:c") shouldEqual "a"
    replaceSingle("20=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("21=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "b"
    replaceSingle("22=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "c"
    replaceSingle("100=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("101=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "b"
    replaceSingle("118=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("120=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "a"
    replaceSingle("121=>cyr0:a|cyr1:b|cyr2:c") shouldEqual "b"
  }

  it should "replace numerics considering cyrillic range option" in {
    replaceSingle("0=>cyr0-1:a|cyr2-5:b|cyr6-9:c") shouldEqual "a"
    replaceSingle("1=>cyr0-1:a|cyr2-5:b|cyr6-9:c") shouldEqual "a"
    replaceSingle("5=>cyr0-1:a|cyr2-5:b|cyr6-9:c") shouldEqual "b"
  }

  it should "replace numerics considering cyrillic range option with number more than 10" in {
    replaceSingle("10=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("11=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("18=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("20=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("21=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "a"
    replaceSingle("22=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "b"
    replaceSingle("100=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("101=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "a"
    replaceSingle("118=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("120=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "c"
    replaceSingle("121=>cyr1:a|cyr2-4:b|cyr5-0:c") shouldEqual "a"
  }

  "multireplace" should "replace multiple simple templates" in {
    service.multireplace(
      "{user} said {curse} {cursesCount} time{cursesCount=>1:|>1:s}. He also said {curseList:and}",
      strings, numerics, lists) shouldEqual
      "Batman said Damn 3 times. He also said word1, word2 and word3"
  }

  it should "replace with nested placeholders" in {
    service.multireplace(
      "{Good day, says {user}!|{user} says hello!} You cursed {cursesCount=>1:once|2:twice|>2:{cursesCount} times}.",
      strings, numerics, lists) should (equal("Good day, says Batman! You cursed 3 times.") or equal("Batman says hello! You cursed 3 times."))
  }

}
