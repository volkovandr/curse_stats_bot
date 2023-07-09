package volkovandr.cursestatsbot.bot

import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update, User}
import volkovandr.cursestatsbot.Logging
import volkovandr.cursestatsbot.configuration.BotConfiguration

import scala.collection.mutable
import scala.jdk.CollectionConverters._

@Component
class Bot(@Value("${apikey}") apiKey: String, config: BotConfiguration) extends TelegramLongPollingBot(apiKey) with Logging {

  private val chats: mutable.Set[Long] = mutable.Set()

  private val cursesPerUser: mutable.Map[(Long, String), mutable.Map[String, Int]] = mutable.Map()

  private val replacementMap = config.replaceLetters.asScala.toMap

  override def onUpdateReceived(update: Update): Unit = {
    if (update.hasMessage && !chats.contains(update.getMessage.getChatId)) {
      chats.add(update.getMessage.getChatId)
      sayHello(update.getMessage.getChatId)
    }
    if (update.hasMessage && update.getMessage.hasText && update.getMessage.getForwardFromMessageId == null) {
      log.trace("Message received: [{}], UpdateId: [{}], Date: [{}], FromId: [{}], ChatId: [{}], Username: [{}], Name: [{} {}]",
        update.getMessage.getText,
        update.getUpdateId,
        update.getMessage.getDate,
        update.getMessage.getFrom.getId,
        update.getMessage.getChatId,
        update.getMessage.getFrom.getUserName,
        update.getMessage.getFrom.getFirstName,
        update.getMessage.getFrom.getLastName
      )
      findCurses(
        update.getMessage.getChatId,
        userName(update.getMessage.getFrom),
        update.getMessage.getText
      )
    }
  }

  private def userName(user: User): String = {
    val name = (Option(user.getFirstName).getOrElse("") + " " + Option(user.getLastName).getOrElse("")).trim
    if (name == "") {
      Option(user.getUserName).getOrElse("")
    } else {
      name
    }
  }

  private def findCurses(chatId: Long, userName: String, message: String): Unit = {
    message
      .split("[^\\p{IsAlphabetic}]")
      .map(_.toLowerCase)
      .foreach { word =>
        log.trace("Checking word: {}", word)
        config.cursesTemplates.find(_.matches(replaceLetters(word))).foreach { _ =>
          val usersMap = cursesPerUser.getOrElseUpdate((chatId, userName), mutable.Map())
          usersMap += (word -> (usersMap.getOrElse(word, 0) + 1))
          log.debug("User {} used curse {}", userName, word)
        }
      }
  }

  private def replaceLetters(word: String): String = replacementMap.foldLeft(word) {
    case (acc, (from, to)) => acc.replace(from, to)
  }

  override def getBotUsername: String = config.botUserName

  private def sayHello(chatId: Long): Unit = if (config.greetingMessage != "") {
    sendMessage(chatId, config.greetingMessage)
  }

  @PreDestroy
  def sayGoodbye(): Unit = if (config.goodbyeMessage != "") {
    sendCurseStats()
    chats
      .map(_.longValue())
      .foreach {
        chatId =>
          sendMessage(chatId, config.goodbyeMessage)
      }
  }

  def clearCurseStats(): Unit = cursesPerUser.clear()

  def sendCurseStats(): Unit = {
    log.debug("Curses stats: {}", cursesPerUser)
    cursesPerUser.groupBy(_._1._1).foreach {
      case (chatId, cursesMap) =>
        cursesMap
          .map(kv => (kv._1._2, kv._2.toList.map(_._2).sum))
          .toList
          .sortBy(_._2)
          .reverse
          .headOption
          .foreach {
            case (userName, cursesCount) =>
              val favoriteCurse = cursesPerUser
                .get((chatId, userName))
                .flatMap(_.toList.sortBy(_._2).reverse.map(_._1).headOption)
                .getOrElse("")
              sendMessage(chatId, config.statsMessage(userName, cursesCount, favoriteCurse))
          }
    }
  }

  private def sendMessage(chatId: Long, text: String): Unit = {
    val message = new SendMessage()
    message.setChatId(chatId)
    message.setText(text)
    try {
      execute[Message, SendMessage](message)
    }
    catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}
