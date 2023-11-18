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

  lazy val cursesTemplates: List[Regex] = curses.asScala
    .map(_.toLowerCase)
    .map(_.r)
    .toList

}
