# hhg-bot
Discord bot for the Haslett High Guild.

This code is fairly stable and shouldn't cause you much trouble. It is most of the code, some was on another computer, 
and I haven't uploaded that yet. However, some stuff has been redacted.

Everything that has been redacted from this repository:
* Tokens for communicating with the Discord API and Gmail API
* DM and email logs
* Verification logs
* Master email and name lists

Everything that is included in this repository:
* Score management
* The simple submission bot which works for 90% of submission cases. For when the simple submission class wasn't able to
 do what the quest called for, I just duped it and named the class the name of the quest.
* Email verification. If you do this, you'll need to configure it with the Gmail API. 
* Channel message counting
* Powerups
* Administrator commands for message 
* Other various utilities
* Ding-donging and baby-sharking

Stuff in the code that I never really used:
* GET requests and a remote MySQL database for score managment (ended up using a local JSON file)

You'll often see `onGuildMessageReceived`. The term `guild` is used by the Discord API to refer to what would be called
"servers."

To complete JDA documentation is [here](https://ci.dv8tion.net/job/JDA/javadoc/).

There are several main() functions in the code, allowing you to run different parts of the code when you need them.

The `HHG.java` class contains many global variables needed for interacting with the server and local files. 

If all you need from all of this is the simple submission bot, here's what you'd need to do to get started from scratch:
1. Install [IntelliJ](https://download.jetbrains.com/idea/ideaIC-2020.1.exe?_ga=2.91195417.26644957.1587419675-592124271.1577055927)
2. [Clone this repo](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html#clone-repo)
3. Run gradle and get a successful build
4. Register a bot in the [Discord API webpage](https://discordapp.com/developers/applications)
5. Add the secret token to HHG.java
6. Edit the parameters in SimpleSubmission.java
7. Run that class
