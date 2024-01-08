# curse_stats_bot
A Telegram Bot that will join a group and count how many times users use bad words

## How to build

`./gradlew jibDockerBuild` will build the docker image that you could just start

## How to test

`./gradlew test` will run the tests

You can also run the bot locally using the following command:

`./gradlew bootRun --args='--apiKey=<your API key>`

```

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
  stats-message-template: |
    Today we cursed {totalCursesCount} times in total. Users {users} are the winners, they cursed {userCursesCount} times each. The words of the day are {favoriteCurses}!
  replaceLetters:
    "[ё]": "е"
  curses:
    - fuck.*
    - shit.*
    - damn.*
    - cunt
    - bitch
  cheating-check-max-curses-per-message: 10
  cheating-check-max-same-curse-per-message: 5
  stats-when-no-curses: true 
  
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

## Configuration

The `greeting-message` is sent to the chat when the bot first time receives any message in a given chat.

The `goodbye-message` is sent berfor bot's shutdoen to each chat where the bot has received a message.

The `stats-message-template` is used to generate everyday statistics message. The following placeholders
can be used in the stats message:
* `{winnerCount}` - indicates the number of users that cursed most of the time.
  It can be zero when nobody cursed at all. It can be one when one user cursed more than others.
  It can be more than one when several users cursed the same number of times.
* `{winnerCursesCount}` - indicates the number of curses that the winner(s) used.
* `{totalCursesCount}` - indicates the total number of curses that were used during the day.
* `{cheatersCount}` - indicates the number of users that were detected as cheaters.
* `{discoveryOfTheDayCount}` - can be 1 or 0 depending on whether the discovery of the day was found.
* `{discoveryOfTheDay}` - indicates the discovery of the day. It is empty when the discovery of the day was not found.
* `{discoveryOfTheDayUser}` - indicates the user that found the discovery of the day. It is empty when the discovery of the day was not found.
* `{favoriteCursesCount}` - indicates the number of the most popular curses. It can be zero when nobody cursed at all.
  It can be one when one curse was used more than others. 
  It can be more than one when several curses were used the same number of times.
* `{winners}` - list of users that cursed most of the time.
* `{favoriteCurses}` - list of the most popular curses.
* `{cheaters}` - list of users that were detected as cheaters.

Additionally, you can customize the stats message using fancy templating. For example you can make the system
pick a random string from a given list of strings, use the following syntax for this: `{Hello|Hi|Good morning}`.

You can customize the lists specifying the separator which will be used betwen the last and the previous element.
By default, it is a comma, but you could use an `and` for example: `{cheaters: and }`. This will be replaced with
something like `User1, User2 and User3`. Note, the list separator should contain spaces, if you want them.

With numerics, you can create expressions that would be evaluated to a different text depending on the value.
For example, you can use the following expression: `Today you cursed {totalCursesCount=><5:not that much.|>=5:quite a lot!}`.

These expressions can be nested, i.e. you can use one of them inside another. For example, you can use random separator in a list as follows:
`{cheaters:{and|and of course|and also|and even}}`

The 'replaceLetters' section allows you to replace some letters in the words before testing them againss the
curses. This can be useful if you want to replace German umlauts with their base letters, for example.

The 'curses' section contains the list of regular expressions that are used to detect curses.

The 'cheating-check-max-curses-per-message' parameter is used to detect cheaters. If a user sends a message
that contains more than this number of curses, then the user is considered a cheater. The curses from this
message will not be counted. And the user will not be considered as a winner even if he/she cursed more than
others in their other messages.

The 'cheating-check-max-same-curse-per-message' parameter is used to detect cheaters. If a user sends a message
that contains the same curse more than this number of times, then he is also considered a cheater.
