package gui;

/**
 * Created by ady on 09/03/16.
 */

import gamelogic.GameController;

/**
 * A class that contains all the settings we need to know
 */
public class GameSettings {

    public long startStack;
    public long bigBlind;
    public long smallBlind;
    public int maxNumberOfPlayers;
    public int levelDuration;
    public GameController.AIType AIType;

    /**
     *
     * @param startStack
     * @param bigBlind
     * @param smallBlind
     * @param maxNumberOfPlayers
     * @param levelDuration
     * @param aiType The type of AI (Simple/MCTS)
     */

    public GameSettings(long startStack, int bigBlind, int smallBlind, int maxNumberOfPlayers, int levelDuration, GameController.AIType aiType) {
        this.startStack = startStack;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.levelDuration = levelDuration;
        this.AIType = aiType;
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

    public long getBigBlind() {
        return bigBlind;
    }

    /**
     * @return smallBlind
     */

    public long getSmallBlind() {
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

    public GameController.AIType getAIType(){ return AIType; }

}
