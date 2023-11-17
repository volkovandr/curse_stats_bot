package volkovandr.cursestatsbot.model

import volkovandr.cursestatsbot.model.Statistics.{ChatId, CursesList, CursesPerUser}

import scala.collection.mutable

class Statistics {
  val cursesPerChatPerUser: mutable.Map[ChatId, CursesPerUser] = mutable.Map()
  val cursesPerChat: mutable.Map[ChatId, CursesList] = mutable.Map()
}

object Statistics {
  type Username = String
  type Curse = String
  type CursesList = Seq[Curse]
  type ChatId = Long
  type CursesPerUser = mutable.Map[Username, CursesList]
}
