package volkovandr.cursestatsbot.service

import org.springframework.stereotype.Service
import volkovandr.cursestatsbot.configuration.BotConfiguration

import scala.util.Random

@Service
class MessageService(config: BotConfiguration) {
  type UserName = String
  type Curse = String
  type PlaceHolder = String

  def greetingMessage: String = config.greetingMessage

  def goodbyeMessage: String = config.goodbyeMessage

  def statsMessage(
                    winners: Seq[UserName],
                    winnerCursesCount: Int,
                    totalCursesCount: Int,
                    favoriteCurses: Seq[Curse],
                    cheaters: Seq[UserName],
                    discoveryOfTheDay: Option[(UserName, Curse)]
                  ): String = multireplace(
    config.statsMessageTemplate,
    Map("discoveryOfTheDay" -> discoveryOfTheDay.map(_._2).getOrElse(""), "discoveryOfTheDayUser" -> discoveryOfTheDay.map(_._1).getOrElse("")),
    Map(
      "winnerCount" -> winners.size,
      "winnerCursesCount" -> winnerCursesCount,
      "totalCursesCount" -> totalCursesCount,
      "cheatersCount" -> cheaters.size,
      "discoveryOfTheDayCount" -> discoveryOfTheDay.map(_ => 1).getOrElse(0),
      "favoriteCursesCount" -> favoriteCurses.size
    ),
    Map(
      "winners" -> winners,
      "favoriteCurses" -> favoriteCurses,
      "cheaters" -> cheaters
    )
  )

  def validateConfig(): Unit = {
    if(config.statsMessageTemplate.count(_ == '{') != config.statsMessageTemplate.count(_ == '}')) {
      throw new RuntimeException("The statsMessageTemplate contains unbalanced curly braces.")
    }
  }

  /**
   * Replaces multiple placeholders in template with values
   *
   * @param template template with placeholders
   * @param strings  map of placeholders and their text values
   * @param numerics map of placeholders and their numeric values
   * @param lists    map of placeholders and their list values
   * @return template with replaced placeholders
   *
   *         The function replaces all the placeholders in the template with values. The placeholders can be nested.
   *         The replacement can be done with text values, numerics or lists. Thus in the provided maps the keys should be
   *         unique event among different types of values.
   *
   *         Simple example with text replacement:
   *         template = "Hello, {user}! You have used the '{word}' curse!"
   *         strings = Map("user" -> "Bob", "word" -> "damn")
   *         numerics = Map()
   *         lists = Map()
   *         result = "Hello, Bob! You have used the 'damn' curse!"
   *
   *
   *         Simple example with numeric replacement:
   *         template = "You have {cursesCount} curses!"
   *         strings = Map()
   *         numerics = Map("cursesCount" -> 3)
   *         lists = Map()
   *         result = "You have 3 curses!"
   *
   *         More complicated way of using numeric placeholders: you can check the value of the numeric and depending on its
   *         value pick the right option.
   *         The syntax is as follows:
   *         {placeholder=>option1:value1|option2:value2|...|optionN:valueN} where option is a numeric value or a range of
   *         values separated by dash, or expressions like `>X` or `>=X` or `<X` or `<=X`.
   *         The value is a string that will be used as a replacement.
   *         template = "You have {cursesCount=>0:no|>0:{cursesCount}} curse{cursesCount=>0:s|1:|>1:s}!"
   *         strings = Map()
   *         numerics = Map("cursesCount" -> 3)
   *         lists = Map()
   *         result = "You have 3 curses!"
   *
   *         You can also use so called cyrillic syntax. This colud be used in languages where the word form depends on the
   *         number. The cyrillic syntax checks the last digit of the number with the exception of numbers between 10 and 20,
   *         whose form is the same as the form of the number 0. The syntax is as folloes:
   *         {placeholder=>cyr1:value1|cyr2-4:value2|cyr5-0:value3} where cyr1, cyr2-4, cyr5-0 are the last digits of the
   *         number. The value is a string that will be used as a replacement.
   *         template = "У тебя {cursesCount} {cursesCount=>cyr1:проклятие|cyr2-4:проклятия|cyr5-0:проклятий}!"
   *         strings = Map()
   *         numerics = Map("cursesCount" -> 3)
   *         lists = Map()
   *         result = "У тебя 3 проклятия!"
   *
   *
   *         Simple example with list replacement:
   *         template = "The following curses where used: {curseList}"
   *         strings = Map()
   *         numerics = Map()
   *         lists = Map("curseList" -> Seq("word1", "word2", "word3"))
   *         result = "The following curses where used: word1, word2, word3"
   *
   *         The list can also contain an ending separator. In this case the last two words will be separated by the
   *         given separator and all other words will be separated by comma. For example:
   *         template = "The following curses where used: {curseList:and}"
   *         strings = Map()
   *         numerics = Map()
   *         lists = Map("curseList" -> Seq("word1", "word2", "word3"))
   *         result = "The following curses where used: word1, word2 and word3"
   *
   *
   *         The function can also pick a random option from the provided list of options. No placeholders are used for that.
   *         For example:
   *         template = "{Hello|Good day} dear friend!"
   *         strings = Map()
   *         numerics = Map()
   *         lists = Map()
   *         result = "Hello dear friend!" or "Good day dear friend!"
   *
   *
   *         Any of these features can be combined, and the expressions in the template can be nested. The parsing is performed
   *         starting from the innermost expressions. For example:
   *         template = "You have {cursesCount=>0:{no|zero|0}|1-5:{{cursesCount}|some|a few}|>5:{cursesCount}} curse{cursesCount=>0:s|1:|>1:s}!"
   *         strings = Map()
   *         numerics = Map("cursesCount" -> 3)
   *         lists = Map()
   *         result = "You have a few curses!"
   */
  // TODO: fix the bug: when a username contains a "{" or "}" character, the function will throw an exception
  def multireplace(template: String, strings: Map[PlaceHolder, String], numerics: Map[PlaceHolder, Int], lists: Map[PlaceHolder, Seq[String]]): String =
    template match {
      case x if x.contains("{") => multireplace(replaceInnerMost(template, strings, numerics, lists), strings, numerics, lists)
      case _ => template
    }

  def replaceInnerMost(template: String, strings: Map[PlaceHolder, String], numerics: Map[PlaceHolder, Int], lists: Map[PlaceHolder, Seq[String]]): String =
    template match {
      case x if x.contains("{") =>
        val start = x.lastIndexOf("{")
        val end = x.indexOf("}", start)
        val inner = x.substring(start + 1, end)
        val replacement = replaceSingle(inner, strings, numerics, lists)
        x.substring(0, start) + replacement + x.substring(end + 1)
      case _ => template
    }

  def replaceSingle(template: String, strings: Map[PlaceHolder, String], numerics: Map[PlaceHolder, Int], lists: Map[PlaceHolder, Seq[String]]): String =
    template match {
      case simpleStringKey if strings.contains(simpleStringKey) => strings(simpleStringKey)
      case simpleNumericKey if numerics.contains(simpleNumericKey) => numerics(simpleNumericKey).toString
      case simpleListKey if lists.contains(simpleListKey) => lists(simpleListKey).mkString(", ")
      case optionalPlaceholder if optionalPlaceholder.contains("|")
        && !numerics.keys.exists(key => optionalPlaceholder.startsWith(key + "=>")) =>
        val options = optionalPlaceholder.split("\\|")
        options(Random.nextInt(options.length))
      case listWithSeparator if listWithSeparator.contains(":")
        && listWithSeparator.split(":").length == 2
        && lists.contains(listWithSeparator.split(":")(0)) =>
        val listKey = listWithSeparator.split(":")(0)
        val separator = listWithSeparator.split(":")(1)
        lists(listKey) match {
          case Nil => ""
          case head :: Nil => head
          case longerList => longerList.dropRight(1).mkString(", ") + " " + separator + " " + longerList.last
        }
      case numericPlaceholder if numericPlaceholder.contains("=>")
        && numericPlaceholder.split("=>").length == 2
        && numerics.contains(numericPlaceholder.split("=>")(0)) =>
        val numericKey = numericPlaceholder.split("=>")(0)
        val options = numericPlaceholder.split("=>")(1).split("\\|")
        val numericValue = numerics(numericKey)
        options.flatMap { option =>
            val criteria = option.split(":")(0)
            val replaceWith = option.dropWhile(_ != ':').drop(1)
            val rangePattern = "(\\d+)-(\\d+)".r
            val greaterPattern = ">(\\d+)".r
            val greaterOrEqualPattern = ">=(\\d+)".r
            val lessPattern = "<(\\d+)".r
            val lessOrEqualPattern = "<=(\\d+)".r
            val equalsPattern = "(\\d+)".r
            val cyrRangePattern = "cyr(\\d+)-(\\d+)".r
            val cyrEqualsPattern = "cyr(\\d+)".r
            criteria match {
              case rangePattern(from, to) if numericValue >= from.toInt && numericValue <= to.toInt => Some(replaceWith)
              case greaterPattern(from) if numericValue > from.toInt => Some(replaceWith)
              case greaterOrEqualPattern(from) if numericValue >= from.toInt => Some(replaceWith)
              case lessPattern(to) if numericValue < to.toInt => Some(replaceWith)
              case lessOrEqualPattern(to) if numericValue <= to.toInt => Some(replaceWith)
              case equalsPattern(value) if numericValue == value.toInt => Some(replaceWith)
              case cyrRangePattern(from, to) if
                numericValue % 100 >= 10 && numericValue % 100 <= 20 && (from.toInt == 0 || to.toInt == 0 || to.toInt == 10) ||
                  numericValue % 100 < 10 && numericValue % 10 >= from.toInt && (numericValue % 10 <= to.toInt || to.toInt == 0) ||
                  numericValue % 100 > 20 && numericValue % 10 >= from.toInt && (numericValue % 10 <= to.toInt || to.toInt == 0) ||
                  numericValue % 10 == 0 && to.toInt == 0 => Some(replaceWith)
              case cyrEqualsPattern(value) if
                numericValue % 100 >= 10 && numericValue % 100 <= 20 && value.toInt == 0 ||
                  numericValue % 100 < 10 && numericValue % 10 == value.toInt ||
                  numericValue % 100 > 20 && numericValue % 10 == value.toInt ||
                  numericValue % 10 == 0 && value.toInt == 0 => Some(replaceWith)

              case _ => None
            }
          }
          .headOption
          .getOrElse(throw new RuntimeException("Cannot replace the placeholder {" + template + "}, it was recognized as a " +
            "numeric placeholder, but the suitable option was not found."))
      case _ => throw new RuntimeException("Cannot replace the placeholder {" + template + "}, it is not an optional placeholder, " +
        "and if it was a key then it was not found in provided maps. Or the syntax is completely wrong.")
    }

}
