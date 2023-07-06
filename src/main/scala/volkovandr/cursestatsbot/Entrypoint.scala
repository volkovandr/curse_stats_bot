package volkovandr.cursestatsbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = Array(
  "volkovandr.cursestatsbot",
  "org.telegram.telegrambots"
))
class Entrypoint

object Entrypoint extends App {
  SpringApplication.run(classOf[Entrypoint], args: _*)
}