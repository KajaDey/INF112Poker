package gui;

/**
 * Created by ady on 09/03/16.
 */

import gamelogic.GameController;

/**
 * A class that contains all the settings we need to know
 */
public class GameSettings {

    private long startStack;
    private long bigBlind;
    private long smallBlind;
    private int maxNumberOfPlayers;
    private int levelDuration;
    private GameController.AIType aiType;

    /**
     *
     * @param startStack
     * @param bigBlind
     * @param smallBlind
     * @param maxNumberOfPlayers
     * @param levelDuration
     * @param aiType The type of AI (Simple/MCTS)
     */

    public GameSettings(long startStack, long bigBlind, long smallBlind, int maxNumberOfPlayers, int levelDuration, GameController.AIType aiType) {
        this.startStack = startStack;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.levelDuration = levelDuration;
        this.aiType = aiType;
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

    public GameController.AIType getAiType(){
        return aiType; }

}
