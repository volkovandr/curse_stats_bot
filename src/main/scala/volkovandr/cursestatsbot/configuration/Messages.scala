package volkovandr.cursestatsbot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

@Configuration
@ConfigurationProperties(prefix = "bot")
class Messages {
  @BeanProperty var greetingMessage: String = ""

  @BeanProperty var statsMessageTemplate: String = _

  @BeanProperty var goodbyeMessage: String = ""

  @BeanProperty var curses: java.util.List[String] = _

  def statsMessage(user: String, cursesCount: Int, favoriteCurse: String): String = {
    statsMessageTemplate
      .replace("{user}", user)
      .replace("{cursesCount}", cursesCount.toString)
      .replace("{favoriteCurse}", favoriteCurse)
  }

  lazy val cursesTemplates: List[Regex] = curses.asScala
    .map(_.toLowerCase)
    .map(_.r)
    .toList
}
