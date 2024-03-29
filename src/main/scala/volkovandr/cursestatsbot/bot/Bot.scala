package volkovandr.cursestatsbot.bot

import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update, User}
import volkovandr.cursestatsbot.Logging
import volkovandr.cursestatsbot.configuration.BotConfiguration
import volkovandr.cursestatsbot.service.{MessageService, StatisticsService, TextParser}

import scala.collection.mutable

@Component
class Bot(
           @Value("${apikey}") apiKey: String,
           config: BotConfiguration,
           textParser: TextParser,
           statsService: StatisticsService,
           messageService: MessageService
         ) extends TelegramLongPollingBot(apiKey) with Logging {

  messageService.validateConfig()

  private val chats: mutable.Set[Long] = mutable.Set()

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
      val curses = textParser.findCurses(
        userName(update.getMessage.getFrom),
        update.getMessage.getText
      )
      if (curses.nonEmpty) {
        if (wasCheating(curses)) {
          log.debug("User {} was cheating with curses {}", userName(update.getMessage.getFrom), curses)
          statsService.reportCheater(update.getMessage.getChatId, userName(update.getMessage.getFrom))
        }
        else
          statsService.addWords(update.getMessage.getChatId, userName(update.getMessage.getFrom), curses)
      }
    }
  }

  def wasCheating(curses: Seq[String]): Boolean = {
    val cursesInMessage = curses.size
    val maxUsedCurseCount = curses.groupBy(identity).map(_._2.size).max
    log.trace("Checking {} for cheating", curses)
    val wasCheating = cursesInMessage >= config.cheatingCheckMaxCursesPerMessage || maxUsedCurseCount >= config.cheatingCheckMaxSameCursePerMessage
    log.trace("CursesInMessage = {}, maxUsedCurseCount = {}. Was cheating: {}", cursesInMessage, maxUsedCurseCount, wasCheating)
    wasCheating
  }

  private def userName(user: User): String = {
    val name = (Option(user.getFirstName).getOrElse("") + " " + Option(user.getLastName).getOrElse("")).trim
    if (name == "") {
      Option(user.getUserName).getOrElse("")
    } else {
      name
    }
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

  def clearCurseStats(): Unit = statsService.clear()

  def sendCurseStats(): Unit = {
    log.debug("Curses stats: {}", statsService.stats.cursesPerChatPerUser)
    chats.filter(chatId => statsService.getTotalNumberOfCurses(chatId) > 0 || config.statsWhenNoCurses).foreach { chatId =>
      val maxCursingUsers = statsService.findMostCursingUsers(chatId)
      val maxCurses = statsService.findMostUsedCurses(chatId)
      val totalCurses = statsService.getTotalNumberOfCurses(chatId)
      val cheaters = statsService.getCheaters(chatId)
      val discoveryOfTheDay = statsService.getDiscoveryOfTheDay(chatId)
      sendMessage(chatId, messageService.statsMessage(
        maxCursingUsers.map(_._1),
        maxCursingUsers.headOption.map(_._2).getOrElse(0),
        totalCurses,
        maxCurses.map(_._1),
        cheaters,
        discoveryOfTheDay
      ))
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
