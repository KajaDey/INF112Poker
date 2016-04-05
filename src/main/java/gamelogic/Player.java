package gamelogic;


/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Player extends User {
    private int ID;
    private long stackSize;
    private long putOnTableThisRound = 0;
    private Card[] holeCards;
    private boolean folded = false;

    //Statistics
    private int finishedInPosition = -1, handsWon = 0, handsPlayed = 0, foldsPreFlop = 0;
    private int numberOfAgressiveMoves = 0, numberOfPassiveMoves = 0;
    private long highestStacksize = 0;
    private Hand bestHand;


    public Player(String name, long stackSize, int ID) {
        super(name);
        this.stackSize = stackSize;
        this.ID = ID;
    }

    /**
     * @return Player ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Execute a given decision
     *
     * @param decision Move to execute
     * @param highestAmountPutOnTable
     */
    public void act(Decision decision, long highestAmountPutOnTable, Pot pot, boolean preflop) {

        long amountToCall = (highestAmountPutOnTable - putOnTableThisRound);
        switch (decision.move) {
            case SMALL_BLIND: case BIG_BLIND:
                putOnTableThisRound = decision.size;
                stackSize -= decision.size;
                pot.addToPot(ID, decision.size);
                break;

            case FOLD:
                numberOfPassiveMoves++;
                if (preflop)
                    foldsPreFlop++;
                this.folded = true;
                break;

            case CHECK:
                numberOfPassiveMoves++;
                break;

            case CALL:
                numberOfPassiveMoves++;
                amountToCall = Math.min(amountToCall, stackSize);
                stackSize -= amountToCall;
                putOnTableThisRound += amountToCall;
                pot.addToPot(ID, amountToCall);
                break;

            case BET:
                assert putOnTableThisRound == 0;
                numberOfAgressiveMoves++;
                this.putOnTableThisRound = decision.size;
                stackSize -= decision.size;
                pot.addToPot(ID, decision.size);
                break;

            case RAISE:
                numberOfAgressiveMoves++;
                stackSize -= (amountToCall + decision.size);
                putOnTableThisRound = highestAmountPutOnTable + decision.size;
                pot.addToPot(ID, amountToCall + decision.size);
                break;

            case ALL_IN:
                numberOfAgressiveMoves++;
                pot.addToPot(ID, stackSize);
                putOnTableThisRound += stackSize;
                stackSize = 0;
                break;
        }
    }

    /**
     * Gets the total amount the player has put on table this round
     *
     * @return Amount put on table
     */
    public long getAmountPutOnTableThisBettingRound() {
        return putOnTableThisRound;
    }

    /**
     * Gets the player's stack size
     *
     * @return Stack size
     */
    public long getStackSize() {
        return stackSize;
    }

    /**
     * Sets the player's hole cards
     *
     * @param card1 Card one in the hand
     * @param card2 Card two in the hand
     */
    public void setHoleCards(Card card1, Card card2) {
        handsPlayed++;
        this.holeCards = new Card[2];
        holeCards[0] = card1;
        holeCards[1] = card2;
    }

    /**
     *  Returns the players hole cards
     * @return Hole cards represented as a Card-array of length 2
     */
    public Card[] getHoleCards() {
        assert holeCards != null : "Hole cards were null when Game asked for them";
        return holeCards;
    }

    /**
     * @return true if the player is still playing, else false
     */
    public boolean stillPlaying() {
        return !folded;
    }

    /**
     * Increments the player's stack with given amount
     *
     * @param size Amount added to stack
     */
    public void incrementStack(long size) {
        stackSize += size;
        if (stackSize > highestStacksize)
            highestStacksize = stackSize;
    }

    /**
     * Called when a new betting round starts, to reset the betting limitations for this player
     */
    public void newBettingRound() {
        this.putOnTableThisRound = 0;
    }

    public void handWon(Hand winningHand) {
        handsWon++;

        //Update best hand (for stats)
        if (bestHand == null)
            bestHand = winningHand;
        else if (winningHand.compareTo(bestHand) > 0)
            bestHand = winningHand;
    }

    public String getBestHand() {
        return "Not implemented in HandCalculator";
    }
}

