package volkovandr.cursestatsbot.service

import org.springframework.stereotype.Service
import volkovandr.cursestatsbot.model.Statistics
import volkovandr.cursestatsbot.model.Statistics.{CursesList, Username}

import scala.collection.mutable

@Service
class StatisticsService {
  val stats = new Statistics()

  def clear(): Unit = {
    stats.cursesPerChat.clear()
  }

  def addWords(chatId: Statistics.ChatId, userName: Statistics.Username, curses: Statistics.CursesList): Unit = {
    val cursesPerUser: mutable.Map[Username, CursesList] = stats.cursesPerChat.getOrElse(chatId, mutable.Map())
    cursesPerUser.put(userName, cursesPerUser.getOrElse(userName, Seq()) ++ curses)

    stats.cursesPerChat.put(chatId, cursesPerUser)
  }

  def findMostCursingUsers(chatId: Statistics.ChatId): Seq[(Username, Int)] = {
    val sorted = stats.cursesPerChat.getOrElse(chatId, Map())
      .map { case (userName, curses) => (userName, curses.size) }
      .toSeq
      .sortBy(-_._2)
    sorted.headOption match {
      case Some((_, max)) => sorted.takeWhile(_._2 == max)
      case None => Seq()
    }
  }

  def findMostUsedCurses(chatId: Statistics.ChatId): Seq[(String, Int)] = {
    val sorted = stats.cursesPerChat.getOrElse(chatId, Map())
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
    stats.cursesPerChat.getOrElse(chatId, Map())
      .map { case (_, curses) => curses.size }
      .sum
  }
}
