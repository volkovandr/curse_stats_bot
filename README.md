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
  goodbye-message: Goodbye everybody, the curse-stats-bot is shutting down!
  stats-message-template-single-user-single-word: Today we cursed {totalCursesCount} times in total. User {user} is the winner, he cursed {userCursesCount} times. The word of the day is {favoriteCurse}!
  stats-message-template-multi-user-single-word: Today we cursed {totalCursesCount} times in total. Users {users} are the winners, they cursed {userCursesCount} times each. The word of the day is {favoriteCurse}!
  stats-message-template-single-user-multi-word: Today we cursed {totalCursesCount} times in total. User {user} is the winner, he cursed {userCursesCount} times. The words of the day are {favoriteCurses}!
  stats-message-template-multi-user-multi-word: Today we cursed {totalCursesCount} times in total. Users {users} are the winners, they cursed {userCursesCount} times each. The words of the day are {favoriteCurses}!
  replaceLetters:
    "[ё]": "е"
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
