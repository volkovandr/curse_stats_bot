package volkovandr.cursestatsbot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.{SendDocument, SendMessage}
import org.telegram.telegrambots.meta.api.objects.{Message, Update}

import java.time.Instant
import scala.collection.mutable

class Bot(apiKey: String) extends TelegramLongPollingBot(apiKey) {

  private val chats: mutable.Set[Long] = mutable.Set()
  override def onUpdateReceived(update: Update): Unit = {
    if(update.hasMessage && update.getMessage.hasText) {
      println("Message received!")
      println(s"UpdateID: ${update.getUpdateId}")
      println(s"Date: ${Instant.ofEpochSecond(update.getMessage.getDate.longValue())}")
      println(s"FromID: ${update.getMessage.getFrom.getId}")
      println(s"ChatID: ${update.getMessage.getChatId}")
      println(s"Username: ${update.getMessage.getFrom.getUserName}")
      println(s"Name: ${update.getMessage.getFrom.getFirstName} ${update.getMessage.getFrom.getLastName}")
      println(s"Message: ${update.getMessage.getText}")
      println("-----------------------------------")
      chats.add(update.getMessage.getChatId)
    }
  }

  override def getBotUsername: String = "CurseStatsBot"

  def sayGoodbye(): Unit = {
    chats
      .map(_.longValue())
      .foreach {
      chatId =>
        val goodbyeMessage = new SendMessage()
        goodbyeMessage.setChatId(chatId)
        goodbyeMessage.setText("Goodbye!")

        try {
          execute[Message, SendMessage](goodbyeMessage)
        }
        catch {
          case e: Throwable => e.printStackTrace()
        }
    }
  }
}
