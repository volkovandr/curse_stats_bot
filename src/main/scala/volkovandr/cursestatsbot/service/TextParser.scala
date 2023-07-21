package volkovandr.cursestatsbot.service

import org.springframework.stereotype.Service
import volkovandr.cursestatsbot.Logging
import volkovandr.cursestatsbot.configuration.BotConfiguration

import scala.jdk.CollectionConverters._

@Service
class TextParser(config: BotConfiguration) extends Logging {
  private val replacementMap = config.replaceLetters.asScala.toMap

  def findCurses(userName: String, message: String): Seq[String] = message
    .split("[^\\p{IsAlphabetic}]")
    .map(_.toLowerCase)
    .flatMap { word =>
      log.trace("Checking word: {}", word)
      config.cursesTemplates.find(_.matches(replaceLetters(word))).map { _ =>
        log.debug("User {} used curse {}", userName, word)
        word
      }
    }

  private def replaceLetters(word: String): String = replacementMap.foldLeft(word) {
    case (acc, (from, to)) => acc.replace(from, to)
  }

}
