package volkovandr.cursestatsbot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

class Bot(apiKey: String) extends TelegramLongPollingBot(apiKey) {
  override def onUpdateReceived(update: Update): Unit = {
    if(update.hasMessage && update.getMessage.hasText) {
      println("Message received!")
      println(s"UpdateID: ${update.getUpdateId}")
      println(s"Date: ${update.getMessage.getDate}")
      println(s"FromID: ${update.getMessage.getForwardFromChat}")
      println(s"ChatID: ${update.getMessage.getChatId}")
      println(s"Username: ${update.getMessage.getFrom.getUserName}")
      println(s"Message: ${update.getMessage.getText}")
      println("-----------------------------------")
    }
  }

  override def getBotUsername: String = "CurseStatsBot"
}
