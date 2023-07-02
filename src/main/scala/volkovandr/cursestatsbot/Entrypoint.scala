package volkovandr.cursestatsbot

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.BotOptions
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object Entrypoint extends App {
  println("Starting...")
  try {
    val botsApi = new TelegramBotsApi(classOf[DefaultBotSession])
    val b = new bot.Bot(args(0))
    val botSession = botsApi.registerBot(b)
    println("Started!")

    sys.addShutdownHook {
      println("Saying goodbye...")
      b.sayGoodbye()
      println("Stopping...")
      botSession.stop()
      println("Stopped!")
    }
  } catch {
    case tex: TelegramApiException => tex.printStackTrace()
    case e: Throwable => throw e
  }
}