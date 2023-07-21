import volkovandr.cursestatsbot.configuration.BotConfiguration
import volkovandr.cursestatsbot.service.TextParser

import scala.jdk.CollectionConverters._
class TextParserTest extends FlatSpecTestBase {

  val config = new BotConfiguration
  config.setCurses(List("shit", "crap", "damn.*", ".*zorro.*", ".*morro.*").asJava)
  config.setReplaceLetters(Map("z" -> "a").asJava)

  val parser = new TextParser(config)

  "TextParserTest" should "find curses in a message of multiple words" in {
    val sampleMessage = "Holy crap, what a damned shit!"
    val foundCurses = parser.findCurses("testUser", sampleMessage)

    foundCurses should contain allOf ("crap", "shit", "damned")
  }

  it should "respect RegEx patterns" in {
    parser.findCurses("", "Shitty crappy text") shouldBe empty
    parser.findCurses("", "damnblabla") should not be empty
  }

  it should "replace letters according to the configuration" in {
    parser.findCurses("", "dzmn crzp!") should contain allOf ("dzmn", "crzp")
  }

  it should "find one word only once even if it matches multiple patters" in {
    parser.findCurses("", "zorromorrozorromorro") should have size(1)
  }

  it should "find same word if it is repeating" in {
    parser.findCurses("", "shit shit shit shit") should have size(4)
  }
}
