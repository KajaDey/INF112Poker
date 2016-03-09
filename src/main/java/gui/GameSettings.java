package main.java.gui;

/**
 * Created by ady on 09/03/16.
 */
public class GameSettings {

    long startChips;
    int bigBlind;
    int smallBlind;
    int amountOfPlayers;
    int levelDuration;

    public GameSettings(long startChips, int bigBlind, int smallBlind, int amountOfPlayers, int levelDuration) {
        this.startChips = startChips;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.amountOfPlayers = amountOfPlayers;
        this.levelDuration = levelDuration;
    }

}
