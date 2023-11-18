package volkovandr.cursestatsbot.service

import org.springframework.stereotype.Service
import volkovandr.cursestatsbot.configuration.BotConfiguration

import scala.util.Random

@Service
class MessageService(config: BotConfiguration) {
  def greetingMessage: String = config.greetingMessage

  def goodbyeMessage: String = config.goodbyeMessage

  def statsMessage(users: Seq[String], userCursesCount: Int, totalCursesCount: Int, favoriteCurses: Seq[String]): String = (users, favoriteCurses) match {
    case (user :: Nil, curse :: Nil) => config.statsMessageTemplateSingleUserSingleWord
      .replace("{user}", user)
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurse}", curse)
    case (user :: Nil, curses) => config.statsMessageTemplateSingleUserMultiWord
      .replace("{user}", user)
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurses}", fancyList(curses))
    case (users, curse :: Nil) => config.statsMessageTemplateMultiUserSingleWord
      .replace("{users}", fancyList(users))
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurse}", curse)
    case (users, curses) => config.statsMessageTemplateMultiUserMultiWord
      .replace("{users}", fancyList(users))
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurses}", fancyList(curses))
  }

  private def fancyList(list: Seq[String]): String = list match {
    case Nil => ""
    case head :: Nil => head
    case head :: tail => tail.mkString(", ") + " and " + head
  }

  def replaceTemplate(template: String, placeholders: Map[String, String]): String = {
    placeholders.foldLeft(template) {
      case (acc, (key, value)) => acc.replace("{" + key + "}", value)
    }
  }

  /**
   * Replaces placeholder with list of values
   * @param template template with placeholder
   * @param list list of values
   * @param placeholder placeholder name
   * @return template with replaced placeholder
   *
   *        The placeholder can be just a string. In this case it will be replaced with list of values separated by comma.
   *        The placeholder can contain an ending separator. In this case the last two words will be separated by the
   *        given separator and all other words will be separated by comma. For example:
   *
   *        template = "The following curses where used: {curseList:and}"
   *        list = Seq("word1", "word2", "word3")
   *        placeholder = "curseList"
   *        result = "The following curses where used: word1, word2 and word3"
   *
   *        It can also contain a list of separators. In that case a random one will be picked by the function. Example:
   *        template = "The following curses where used: {curseList:and,or,or maybe,as well}"
   *        list = Seq("word1", "word2", "word3")
   *        placeholder = "curseList"
   *        possible result = "The following curses where used: word1, word2 or word3"
   *        another possible result = "The following curses where used: word1, word2 or maybe word3"
   *
   *        It can also support multiple placeholders in one template, all of them will be replaced.
   */
  def replateList(template: String, list: Seq[String], placeholder: String): String = {
    val phPattern = ("\\{" + placeholder + "(:(.+?))?\\}").r
    phPattern.replaceAllIn(template, m => {
      val separator = Option(m.group(2))
        .map(_.split(",").toList)
        .map(l => " " + l(Random.nextInt(l.size)) + " ")
        .getOrElse(", ")
      list match {
        case Nil => ""
        case head :: Nil => head
        case head :+ tail => head.mkString(", ") + separator + tail
      }
    })
  }
}
