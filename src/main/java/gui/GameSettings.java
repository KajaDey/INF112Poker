package main.java.gui;

/**
 * Created by ady on 09/03/16.
 */

/**
 * A class that contains all the settings we need to know
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

    /**
     * @return startStack
     */

    public long getStartStack() {
        return startStack;
    }

    /**
     * @return bigBlind
     */

    public int getBigBlind() {
        return bigBlind;
    }

    /**
     * @return smallBlind
     */

    public int getSmallBlind() {
        return smallBlind;
    }

    /**
     * @return maxNumberOfPlayers
     */
    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    /**
     * @return levelDuration
     */
    public int getLevelDuration() {
        return levelDuration;
    }
}
