package gui;
import gamelogic.AIType;
import gamelogic.GameController;

/**
 * A class that contains all the settings in a game
 *
 * @author André Dyrstad
 */
public class GameSettings {

    public final static GameSettings DEFAULT_SETTINGS = new GameSettings(5000, 50, 25, 6, 10, AIType.MCTS_AI,30);

    private long startStack;
    private long bigBlind;
    private long smallBlind;
    private int maxNumberOfPlayers;
    private int levelDuration;
    private AIType aiType;
    private int playerClock;

    /**
     *
     * @param startStack
     * @param bigBlind
     * @param smallBlind
     * @param maxNumberOfPlayers
     * @param levelDuration
     * @param aiType The type of AI (Simple/MCTS)
     */

    public GameSettings(long startStack, long bigBlind, long smallBlind, int maxNumberOfPlayers, int levelDuration, AIType aiType, int playerClock) {
        this.startStack = startStack;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.levelDuration = levelDuration;
        this.aiType = aiType;
        this.playerClock = playerClock;
    }

    public GameSettings(GameSettings oldSetting) {
        startStack = oldSetting.startStack;
        bigBlind = oldSetting.bigBlind;
        smallBlind = oldSetting.smallBlind;
        maxNumberOfPlayers = oldSetting.maxNumberOfPlayers;
        levelDuration = oldSetting.levelDuration;
        aiType = oldSetting.aiType;
    }

    /**
     * Doubles the blinds
     */
    public void increaseBlinds() {
        smallBlind *= 2;
        bigBlind *= 2;
    }


    public void setStartStack(long startStack) {
        this.startStack = startStack;
    }

    public void setBigBlind(long bigBlind) {
        this.bigBlind = bigBlind;
    }

    public void setSmallBlind(long smallBlind) {
        this.smallBlind = smallBlind;
    }

    public void setMaxNumberOfPlayers(int maxNumberOfPlayers) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
    }

    public void setLevelDuration(int levelDuration) {
        this.levelDuration = levelDuration;
    }

    public void setAiType(AIType aiType) {
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

    public AIType getAiType(){
        return aiType; }

    public int getPlayerClock(){
        return playerClock;
    }

    public String toString(){
        return startStack+ "\n" + bigBlind + "\n" + smallBlind + "\n" + maxNumberOfPlayers + "\n" +
                levelDuration + "\n" + aiType + "\n" + playerClock + "\n";
    }
}
