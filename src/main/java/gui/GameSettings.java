package gui;
import gamelogic.AIType;

/**
 * A class that contains all the settings in a game
 *
 * @author André Dyrstad
 */
public class GameSettings {

    private final static GameSettings DEFAULT_SETTINGS = new GameSettings(5000, 100, 50, 6, 5, AIType.MCTS_AI, 30);

    private long startStack;
    private long bigBlind;
    private long smallBlind;
    private int maxNumberOfPlayers;
    private int levelDuration;
    private AIType aiType;
    private int playerClock;

    public GameSettings() {
        this(DEFAULT_SETTINGS);
    }

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
        playerClock = oldSetting.playerClock;
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

    public void setPlayerClock(int playerClock) { this.playerClock = playerClock; }

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

    /**
     *  Checks for errors in the game settings
     *  @return The appropriate error message if there is an error, 'No error' otherwise
     */
    public String getErrorMessage() {
        String error = "No error";
        if (startStack < 0) {
            error = "Start stack must be a positive whole number";
        } else if (startStack < bigBlind * 10){
            error = "Start stack must be at least 10 times the big blind, is " + this.getStartStack() + " with big blind " + this.getBigBlind();
        } else if(bigBlind < 0 || smallBlind < 0) {
            error = "All blinds must be positive whole numbers";
        } else if (bigBlind < smallBlind * 2) {
            error = "Big blind (" + bigBlind + ") must be at least twice the size of the small blind (" + smallBlind + ")";
        } else if(maxNumberOfPlayers< 2 || maxNumberOfPlayers > 6) {
            error = "Number of players must be between 2-6";
        } else if(bigBlind <= 0) {
            error = "Blind level must be a positive whole number";
        } else if (playerClock < 5 || playerClock > 60)
            error = "The player clock must be between 5 and 60 seconds";

        return error;
    }

    public boolean valid() {
        return getErrorMessage().equalsIgnoreCase("no error");
    }
}
