# curse_stats_bot
A Telegram Bot that will join a group and count how many times users use bad words

## How to build

`./gradlew jibDockerBuild` will build the docker image that you could just start

## How to run

Create a directoy where you would like to store the config file

`mkdir /opt/curse_stats_bot`

Create a config file in that directory

`vim /opt/curse_stats_bot/application-docker.yml`

```yaml
spring.profiles.active: docker

printStatsCron: 0 0 0 * * ?

logging:
  level:
    volkovandr: info

bot:
  greeting-message: Hello everybody, the curse-stats-bot is running!
  stats-message-template: User {user} cursed {cursesCount} times, his favorite curse is {favoriteCurse}!
  goodbye-message: Goodbye everybody, the curse-stats-bot is shutting down!
  curses:
    - fuck.*
    - shit.*
    - damn.*
    - cunt
    - bitch
  
apikey: <your API key>
```

The use the following command to mount the current directory to the container's directory,
update the entrypoint and run the container:

```
docker run \
    --restart unless-stopped \
    --name curse-stats-bot \
    --volume=/opt/curse_stats_bot:/opt/curse_stats_bot:ro \
    -d \
    cursestatsbot \
    --spring.config.location=file:///opt/curse_stats_bot/application-docker.yml
```
