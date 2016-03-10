package main.java.gui;

/**
 * Created by ady on 09/03/16.
 */
public class GameSettings {

    public long startStack;
    public int bigBlind;
    public int smallBlind;
    public int maxNumberOfPlayers;
    public int levelDuration;

    public GameSettings(long startStack, int bigBlind, int smallBlind, int maxNumberOfPlayers, int levelDuration) {
        this.startStack = startStack;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.levelDuration = levelDuration;
    }

    public long getStartStack() {
        return startStack;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    public int getLevelDuration() {
        return levelDuration;
    }
}
