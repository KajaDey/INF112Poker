# INTRODUCTION
---
This is a project in context with a subject at the University in Bergen, Norway.
The subject is "Software Engineering", code INF112 ([info can be found here](http://www.uib.no/en/course/INF112)),
and this years task is to engineer an implementation of Texas Hold'em poker, over three sprints, using unified process (UP).

* The project is using Maven. The code is written in java, GUI is written using the JavaFX library.

##Authors
---
    André Dyrstad
    Henrik Nytun
    Jostein Kringlen
    Kaja Dey
    Kristian Rosland
    Mariah Vaarum
    Morten Lohne
    Ragnhild Aalvik
    Simon Halvorsen
    Vegar Rorvik

* For this agile project, we have an online Scrum board:
  * [Scrum board](https://scrumy.com/inf112gruppe4)
* We also communicate using Slack.
* We use bitbuckets bug tracker, "Issues", to report bugs when we encounter them, while playing the game.
* Documentation like minutes, diagrams etc. can be found under docs/ in our repo.

How to play:

Singleplayer:
This game mode allows a player to play against 1-5 AI's.
Click the singleplayer button in the Main screen. The user then enters a name of his/hers choise and presses "enter".
The player can now edit settings or choose to play with default settings. When the user is ready, he/she clicks "start game".

Multiplayer:
This game mode allow a player to play against 1-5 players. The players could be either a human player or an AI. AI's will be
added to the game if the game is started with fewer players than what is set in the settings. To play a multiplayer game, a server
has to be up and running. By only entering a name in the Main screen and pressing enter, the player will connect to a remote
server. If a payer wants to join another server, he/she can enter the IP address of the player hosting the server and press
enter, to connect. Any player connected to a server can make a lobby for others to join. Only the lobby host can edit the settings
and delete his/hers lobby.

Short keys:
 -Check: SPACE x2
 -Bet/Raise: SHIFT + ENTER
 -Fold: SPACE
 -Raise bet by 1xBB: ARROW UP
 -Lower bet by 1xBB: ARROW DOWN
 -Raise bet by 10xBB: SHIFT + ARROW UP
 -Lower bet by 10xBB: SHIFT + ARROW DOWN


## Usage of software
---
* To try our software, either for development reasons and/or to try the program you can clone the project using git. You must have java 1.8 and maven installed.
Our team is using Intellij, and by using that, you can easily import this project using Import from Version Control.

* You can run tests using the Maven project, with the lifecycle point called test.

* To run the program, either, from IntelliJ, run GUIMain, or run package under Maven, open target folder in Finder/Explorer/Files
(depends on OS), and run the file inf112v16-g4-Poker.jar

* This implementation of Texas Hold'em allows the user to play a game of heads-up poker against an AI.
The user can set the preferred game settings and the game continues until one of the players are out of chips.
Later implementations will allow the user to create a multiplayer game and play against other human
players over the internet. This implementation will also allow for a user to spectate a table of players.

###Bug tracking
If you where to test the software, and experience bugs while playing, feel free to add it to our bug tracker in our
bitbucket repo, under "Issues". We use this feature to report bugs, and to assign the bug to whomever who can fix and/or
is in charge of that particular part of the program.

**Link to bug tracker:** https://bitbucket.org/tha056/inf112v16-g4/issues

# LICENSE
---
This project is free software under GNU General Public License 3, **EXCEPT** for the Card Sprites.
Look beneath our license to see the license for the **Card Sprites**.

This implementation of Texas Hold'em is a project in the subject "Software Engineering" at the University in Bergen, Norway.

Copyright (C) 2016
Kristian Rosland, Morten Lohne, Jostein Kringlen, Mariah Vårum, Simon Halvorsen, Kaja Dey, André Dyrstad, Henrik Nytun, Ragnhild Aalvik, Vegar Rørvik

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/

## CardSprites license
---
--------------------------- http://wheels-cards.wc.lt/ ---------------------------

High quality PNG playing cards - 537x750

Terms of Use:

- These playing cards (cards, joker, ace of spades, card back, card box) are not intended for commercial use and to obtain any commercial benefits!
- These playing cards can not be printed!
- Personal use only!

--------------------------- http://wheels-cards.wc.lt/ ---------------------------

## Mockito license
---
--------------------------- https://opensource.org/licenses/MIT ------------------

PowerMockito (extension to Mockito) is a testing framework released under the MIT License. We used PowerMockito to make it easier to test game logic and networking.
Using PowerMockito we were able to write fast unit tests by removing GUI when simulating games, and by removing hard coded delays inserted to make the user experience 
smoother. 

Copyright (c) 2016 Mockito

    Permission is hereby granted, free of charge, to any person obtaining
    a copy of this software and associated documentation files (the "Software"),
    to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the 
    Software is furished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included
    in all copies of substantial portions of the Software.

--------------------------- https://opensource.org/licenses/MIT ------------------