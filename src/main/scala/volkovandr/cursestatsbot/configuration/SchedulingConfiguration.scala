package volkovandr.cursestatsbot.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import volkovandr.cursestatsbot.bot.Bot

@EnableScheduling
@Configuration
class SchedulingConfiguration(
                             bot: Bot
                             ) {
  @Scheduled(cron = "${printStatsCron}")
  def printStats(): Unit = {
    bot.sendCurseStats()
    bot.clearCurseStats()
  }
}