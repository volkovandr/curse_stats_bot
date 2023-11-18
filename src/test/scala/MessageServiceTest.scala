import volkovandr.cursestatsbot.configuration.BotConfiguration
import volkovandr.cursestatsbot.service.MessageService

class MessageServiceTest extends FlatSpecTestBase {
  val config = new BotConfiguration()
  val service = new MessageService(config)

  "MessageService" should "replace template" in {
    val template = "Hello, {user}! You have {cursesCount} curses!"
    val placeholders = Map("user" -> "Vasya", "cursesCount" -> "3")
    val expected = "Hello, Vasya! You have 3 curses!"
    val actual = service.replaceTemplate(template, placeholders)
    actual shouldEqual expected
  }

  it should "replace template even if same template appears multiple times" in {
    val template = "Hello, {user}! You have {cursesCount} curses! {user}! You have {cursesCount} curses!"
    val placeholders = Map("user" -> "Vasya", "cursesCount" -> "3")
    val expected = "Hello, Vasya! You have 3 curses! Vasya! You have 3 curses!"
    val actual = service.replaceTemplate(template, placeholders)
    actual shouldEqual expected
  }

  it should "replace template even if template is empty" in {
    val template = ""
    val placeholders = Map("user" -> "Vasya", "cursesCount" -> "3")
    val expected = ""
    val actual = service.replaceTemplate(template, placeholders)
    actual shouldEqual expected
  }

  it should "replace list" in {
    val template = "The following curses where used: {curseList:and}"
    val list = Seq("word1", "word2", "word3")
    val actual = service.replateList(template, list, "curseList")
    val expected = "The following curses where used: word1, word2 and word3"
    actual shouldEqual expected
  }

  it should "replace list that appears multiple times" in {
    val template = "The following curses where used: {curseList:and} and {curseList:or}"
    val list = Seq("word1", "word2", "word3")
    val actual = service.replateList(template, list, "curseList")
    val expected = "The following curses where used: word1, word2 and word3 and word1, word2 or word3"
    actual shouldEqual expected
  }

  it should "replace list when the separator not proivider" in {
    val template = "The following curses where used: {curseList}"
    val list = Seq("word1", "word2", "word3")
    val actual = service.replateList(template, list, "curseList")
    val expected = "The following curses where used: word1, word2, word3"
    actual shouldEqual expected
  }

  it should "replace list when the list is empty" in {
    val template = "The following curses where used: {curseList}"
    val list = Seq()
    val actual = service.replateList(template, list, "curseList")
    val expected = "The following curses where used: "
    actual shouldEqual expected
  }

  it should "pick a random separator when multiple of them provided" in {
    val template = "The following curses where used: {curseList:and,or}"
    val list = Seq("word1", "word2", "word3")
    val actual = service.replateList(template, list, "curseList")
    val expected1 = "The following curses where used: word1, word2 or word3"
    val expected2 = "The following curses where used: word1, word2 and word3"
    actual should (equal(expected1) or equal(expected2))
  }


}
