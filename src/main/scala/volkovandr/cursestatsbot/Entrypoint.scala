package volkovandr.cursestatsbot

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object Entrypoint extends App {
  println("Starting...")
  try {
    val botsApi = new TelegramBotsApi(classOf[DefaultBotSession])
    botsApi.registerBot(new bot.Bot(args(0)))
    println("Started!")
  } catch {
    case tex: TelegramApiException => tex.printStackTrace()
    case e => throw e
  }
}