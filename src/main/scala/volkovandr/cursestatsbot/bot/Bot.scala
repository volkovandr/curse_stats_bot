package volkovandr.cursestatsbot.bot

import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import volkovandr.cursestatsbot.Logging
import volkovandr.cursestatsbot.configuration.Messages

import scala.collection.mutable

@Component
class Bot(@Value("${apikey}") apiKey: String, messages: Messages) extends TelegramLongPollingBot(apiKey) with Logging {

  private val chats: mutable.Set[Long] = mutable.Set()

  private val cursesPerUser: mutable.Map[(Long, String), mutable.Map[String, Int]] = mutable.Map()

  override def onUpdateReceived(update: Update): Unit = {
    if (update.hasMessage && update.getMessage.hasText) {
      //      println("Message received!")
      //      println(s"UpdateID: ${update.getUpdateId}")
      //      println(s"Date: ${Instant.ofEpochSecond(update.getMessage.getDate.longValue())}")
      //      println(s"FromID: ${update.getMessage.getFrom.getId}")
      //      println(s"ChatID: ${update.getMessage.getChatId}")
      //      println(s"Username: ${update.getMessage.getFrom.getUserName}")
      //      println(s"Name: ${update.getMessage.getFrom.getFirstName} ${update.getMessage.getFrom.getLastName}")
      //      println(s"Message: ${update.getMessage.getText}")
      //      println("-----------------------------------")
      if (!chats.contains(update.getMessage.getChatId)) {
        chats.add(update.getMessage.getChatId)
        sayHello(update.getMessage.getChatId)
      }
      if (update.getMessage.getForwardFromMessageId == null) {
        findCurses(
          update.getMessage.getChatId,
          Option(update.getMessage.getFrom.getUserName)
            .getOrElse(s"${
              Option(update.getMessage.getFrom.getFirstName).getOrElse("")
            } ${
              Option(update.getMessage.getFrom.getLastName).getOrElse("")
            }").trim,
          update.getMessage.getText
        )
      }
    }
  }

  private def findCurses(chatId: Long, userName: String, message: String): Unit = {
    message
      .split("\\W+")
      .map(_.toLowerCase)
      .foreach { word =>
        messages.cursesTemplates.foreach { curse =>
          if (curse.matches(word)) {
            val usersMap = cursesPerUser.getOrElseUpdate((chatId, userName), mutable.Map())
            usersMap += (word -> (usersMap.getOrElse(word, 0) + 1))
            log.debug("User {} used curse {}", userName, word)
          }
        }
      }
  }

  override def getBotUsername: String = "CurseStatsBot"

  private def sayHello(chatId: Long): Unit = if (messages.greetingMessage != "") {
    sendMessage(chatId, messages.greetingMessage)
  }

  @PreDestroy
  def sayGoodbye(): Unit = if (messages.goodbyeMessage != "") {
    sendCurseStats()
    chats
      .map(_.longValue())
      .foreach {
        chatId =>
          sendMessage(chatId, messages.goodbyeMessage)
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
              sendMessage(chatId, messages.statsMessage(userName, cursesCount, favoriteCurse))
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
