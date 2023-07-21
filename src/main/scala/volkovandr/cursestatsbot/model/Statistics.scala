package volkovandr.cursestatsbot.model

import volkovandr.cursestatsbot.model.Statistics.{ChatId, CursesPerUser}

import scala.collection.mutable

class Statistics {
  val cursesPerChat: mutable.Map[ChatId, CursesPerUser] = mutable.Map()
}

object Statistics {
  type Username = String
  type CursesList = Seq[String]
  type ChatId = Long
  type CursesPerUser = mutable.Map[Username, CursesList]
}
