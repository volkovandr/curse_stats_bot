package volkovandr.cursestatsbot.service

import org.springframework.stereotype.Service
import volkovandr.cursestatsbot.model.Statistics
import volkovandr.cursestatsbot.model.Statistics.{ChatId, CursesList, Username}

import scala.collection.mutable

@Service
class StatisticsService {
  val stats = new Statistics()
  val discoveryOfTheDay: mutable.Map[Statistics.ChatId, (Statistics.Username, Statistics.Curse)] = mutable.Map()

  def clear(): Unit = {
    stats.cursesPerChatPerUser.clear()
    stats.cheatersPerChat.clear()
    discoveryOfTheDay.clear()
  }

  def reportCheater(chatId: Statistics.ChatId, userName: Statistics.Username): Unit = {
    stats.cheatersPerChat.put(chatId, stats.cheatersPerChat.getOrElse(chatId, mutable.Set()) + userName)
  }

  def addWords(chatId: Statistics.ChatId, userName: Statistics.Username, curses: Statistics.CursesList): Unit = {
    val cursesPerUser: mutable.Map[Username, CursesList] = stats.cursesPerChatPerUser.getOrElse(chatId, mutable.Map())
    cursesPerUser.put(userName, cursesPerUser.getOrElse(userName, Seq()) ++ curses)

    stats.cursesPerChatPerUser.put(chatId, cursesPerUser)
    curses.foreach { curse =>
      if(!stats.cursesPerChat.contains(chatId)) {
        stats.cursesPerChat.put(chatId, Seq())
      }
      if(!stats.cursesPerChat(chatId).contains(curse) && !discoveryOfTheDay.contains(chatId)) {
        discoveryOfTheDay.put(chatId, (userName, curse))
      }
      stats.cursesPerChat.put(chatId, stats.cursesPerChat(chatId) :+ curse)
    }
  }

  def findMostCursingUsers(chatId: Statistics.ChatId): Seq[(Username, Int)] = {
    val sorted = stats.cursesPerChatPerUser.getOrElse(chatId, Map())
      .map { case (userName, curses) => (userName, curses.size) }
      .filterNot(kv => stats.cheatersPerChat.get(chatId).exists(_.contains(kv._1)))
      .toSeq
      .sortBy(-_._2)
    sorted.headOption match {
      case Some((_, max)) => sorted.takeWhile(_._2 == max)
      case None => Seq()
    }
  }

  def findMostUsedCurses(chatId: Statistics.ChatId): Seq[(String, Int)] = {
    val sorted = stats.cursesPerChatPerUser.getOrElse(chatId, Map())
      .toSeq
      .flatMap { case (_, curses) => curses }
      .groupBy(identity)
      .map(kv => (kv._1, kv._2.size))
      .toSeq
      .sortBy(-_._2)
    sorted.headOption match {
      case Some((_, max)) => sorted.takeWhile(_._2 == max)
      case None => Seq()
    }
  }

  def getTotalNumberOfCurses(chatId: Statistics.ChatId): Int = {
    stats.cursesPerChatPerUser.getOrElse(chatId, Map())
      .map { case (_, curses) => curses.size }
      .sum
  }

  def getCheaters(chatId: ChatId): Seq[Username] = stats.cheatersPerChat.getOrElse(chatId, Seq()).toSeq

  def getDiscoveryOfTheDay(chatId: Statistics.ChatId): Option[(Statistics.Username, Statistics.Curse)] = discoveryOfTheDay.get(chatId)

  def removeChat(chatId: ChatId): Unit = {
    stats.cursesPerChatPerUser.remove(chatId)
    stats.cheatersPerChat.remove(chatId)
    stats.cursesPerChat.remove(chatId)
    discoveryOfTheDay.remove(chatId)
  }
}
