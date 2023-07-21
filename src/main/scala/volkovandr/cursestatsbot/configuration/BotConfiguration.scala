package volkovandr.cursestatsbot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

@Configuration
@ConfigurationProperties(prefix = "bot")
class BotConfiguration {
  @BeanProperty var greetingMessage: String = ""

  @BeanProperty var statsMessageTemplateSingleUserSingleWord: String = _
  @BeanProperty var statsMessageTemplateMultiUserSingleWord: String = _
  @BeanProperty var statsMessageTemplateSingleUserMultiWord: String = _
  @BeanProperty var statsMessageTemplateMultiUserMultiWord: String = _

  @BeanProperty var goodbyeMessage: String = ""

  @BeanProperty var replaceLetters: java.util.Map[String, String] = Map[String, String]().asJava

  @BeanProperty var curses: java.util.List[String] = _

  @BeanProperty var botUserName: String = ""

  def statsMessage(users: Seq[String], userCursesCount: Int, totalCursesCount: Int, favoriteCurses: Seq[String]): String = (users, favoriteCurses) match {
    case (user :: Nil, curse :: Nil) => statsMessageTemplateSingleUserSingleWord
      .replace("{user}", user)
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurse}", curse)
    case (user :: Nil, curses) => statsMessageTemplateSingleUserMultiWord
      .replace("{user}", user)
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurses}", fancyList(curses))
    case (users, curse :: Nil) => statsMessageTemplateMultiUserSingleWord
      .replace("{users}", fancyList(users))
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurse}", curse)
    case (users, curses) => statsMessageTemplateMultiUserMultiWord
      .replace("{users}", fancyList(users))
      .replace("{userCursesCount}", userCursesCount.toString)
      .replace("{totalCursesCount}", totalCursesCount.toString)
      .replace("{favoriteCurses}", fancyList(curses))
  }

  lazy val cursesTemplates: List[Regex] = curses.asScala
    .map(_.toLowerCase)
    .map(_.r)
    .toList

  private def fancyList(list: Seq[String]): String = list match {
    case Nil => ""
    case head :: Nil => head
    case head :: tail => tail.mkString(", ") + " and " + head
  }
}
