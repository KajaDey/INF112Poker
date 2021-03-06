package gamelogic;

import gui.GUIMain;
import gui.GameScreen;
import gui.GameSettings;
import network.Server;

import java.util.*;

/**
 * Created by Kristian Rosland on 07.03.2016.
 *
 *  Class representing the playing of a game. Contains all players, stack sizes etc.
 *  Uses GameController to get decision from clients corresponding to players (by ID)
 */
public class Game {

    public static final long WAIT_FOR_COMMUNITY_CARD_DELAY = 1000L;
    public static final long WAIT_FOR_COMMUNITY_CARD_ALL_IN_DELAY = 4000L;
    public static final long HAND_OVER_DELAY = 6000L;
    public static final long EVERYONE_FOLDED_DELAY = 3000L;

    //Controls
    private Player[] players;
    private GameController gameController;
    private GameSettings gameSettings;

    //Settings
    private int numberOfPlayers = 0, remainingPlayers = 0, finishedInPosition;
    private long lastBlindRaiseTime = 0;

    //Indexes
    private int smallBlindIndex = 0;

    //Round specific
    private Pot pot;
    private int roundNumber = 0;
    private long highestAmountPutOnTable = 0, currentMinimumRaise = 0;
    private Map<Integer, Long> stackSizes;
    private List<Player> playersStillInCurrentHand;
    private Map<Integer, Card[]> holeCards;
    private Map<Integer, Integer> positions;
    private Map<Integer, Integer> rankingTable;
    private Map<Integer, String> names;
    private Card[] communityCards;
    private Deck deck;
    private ArrayDeque<Card> cardQueue;

    //Replay
    private boolean replay = false;
    private Optional<Queue<Long>> replayTimeQueue = Optional.empty();

    private final Logger logger;

    public Game(GameSettings gameSettings, GameController gameController, Logger logger) {
        this.gameController = gameController;
        this.gameSettings = gameSettings;
        this.logger = logger;

        this.finishedInPosition = gameSettings.getMaxNumberOfPlayers();
        this.players = new Player[gameSettings.getMaxNumberOfPlayers()];
        this.stackSizes = new HashMap<>();
        this.rankingTable = new HashMap<>();
        this.names = new HashMap<>();
        this.pot = new Pot(logger);
    }

    /**
     * Adds a new player to the game. The player is given a start stack size.
     *
     * @param name Name of the player
     * @param ID   Player-ID
     * @return true if player was added successfully, else false
     */
    public boolean addPlayer(String name, int ID) {
        if (numberOfPlayers >= gameSettings.getMaxNumberOfPlayers()) {
            return false;
        }

        Player p = new Player(name, gameSettings, ID);
        for (int i = 0; i < gameSettings.getMaxNumberOfPlayers(); i++) {
            if (players[i] == null) {
                players[i] = p;
                numberOfPlayers++;
                remainingPlayers++;
                gameController.setCallback(p.getID(), p::showCards);
                break;
            }
        }
        stackSizes.put(ID, gameSettings.getStartStack());
        names.put(ID, name);

        return true;
    }

    /**
     * Plays a game until a player has won.
     */
    public void playGame() {
        lastBlindRaiseTime = getTime();
        logger.replayLogPrint("\nTIME\n"+lastBlindRaiseTime);

        while (numberOfPlayersWithChipsLeft() > 1) {
            logger.println("\nNew hand", Logger.MessageType.GAMEPLAY);
            //Tell all clients that a new hand has started and update all players stack sizes
            refreshAllStackSizes();

            //Get an ordered list of players in the current hand (order BTN, SB, BB...)
            playersStillInCurrentHand = getOrderedListOfPlayersStillPlaying();
            gameController.startNewHand();
            refreshAllStackSizes();
            gameController.setPositions(positions);

            pot = new Pot(logger);

            //Deal all hole cards and save community cards for later use
            deck = new Deck();
            communityCards = generateCommunityCards();
            dealHoleCards(playersStillInCurrentHand);

            playHand();
        }

        //Deal with who won the game.. (should be the only player with chips left
        assert numberOfPlayersWithChipsLeft() == 1 : "Game over but " + numberOfPlayersWithChipsLeft() + " had chips left";
        pot = new Pot(logger);
        refreshAllStackSizes();

        gameOver();
    }

    /**
     * Play one hand (until showdown or all players but 1 has folded)
     */
    private void playHand() {
        boolean preFlop = true;
        //Makes the small and big blind pay their blind by forcing an act. Updates stackSizes

        long currentTime = getTime();
        logger.println("\nBLINDS (Small " + this.gameSettings.getSmallBlind() + ", big " + this.gameSettings.getBigBlind() + ")", Logger.MessageType.GAMEPLAY);
        logger.replayLogPrint("\nTIME\n"+currentTime);

        // Increase blinds
        if (currentTime - (gameSettings.getLevelDuration() * 60 * 1000) > lastBlindRaiseTime) {
            gameSettings.increaseBlinds();
            logger.println("Blinds increased to " + gameSettings.getSmallBlind() + ", " + gameSettings.getBigBlind(), Logger.MessageType.GAMEPLAY);
            gameController.setBlinds();
            lastBlindRaiseTime = currentTime;
        }

        postBlinds();
        refreshAllStackSizes();
        printAllPlayerStacks();

        //First betting round (pre flop)
        logger.println("\nPRE FLOP:", Logger.MessageType.GAMEPLAY);
        boolean handContinues = bettingRound(preFlop);
        if (skipBettingRound() && playersStillInCurrentHand.size() > 1) {
            displayHoleCards();
            delay(WAIT_FOR_COMMUNITY_CARD_ALL_IN_DELAY);
        }
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display flop and new betting round
        logger.println("\nFLOP:", Logger.MessageType.GAMEPLAY);
        setFlop();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display turn and new betting round
        logger.println("\nTURN:", Logger.MessageType.GAMEPLAY);
        setTurn();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display river and new betting round
        logger.println("\nRIVER:", Logger.MessageType.GAMEPLAY);
        setRiver();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Showdown
        showdown();
    }


    /**
     * Play one complete betting round (where all players act until everyone agree (or everyone but 1 folds))
     *
     * @param isPreFlop True if this is the pre flop betting round
     * @return True if the hand continues, false if the hand is over
     */
    private boolean bettingRound(boolean isPreFlop) {
        logger.replayLogPrint("\nDECISIONS");
        //Determine who is acting first (based on the isPreFLop-value)
        int actingPlayerIndex;

        if (!isPreFlop && remainingPlayers == 2)
            actingPlayerIndex = 1;
        else
            actingPlayerIndex = (isPreFlop ? 2 % playersStillInCurrentHand.size() : 0);

        highestAmountPutOnTable = (isPreFlop ? gameSettings.getBigBlind() : 0);
        currentMinimumRaise = gameSettings.getBigBlind();

        if (!isPreFlop) {
            for (Player p : players)
                p.newBettingRound();
        }

        //Check if all players are all in and betting round should be skipped
        if (skipBettingRound()) {
            displayHoleCards();
            delay(WAIT_FOR_COMMUNITY_CARD_ALL_IN_DELAY);
            return true;
        }

        //Return true if hand continues, false if hand is over
        boolean handContinues = getDecisions(actingPlayerIndex, isPreFlop);
        delay(WAIT_FOR_COMMUNITY_CARD_DELAY);
        return handContinues;
    }

    /**
     * Get decision from all players until all agressive moves have been responded to
     *
     * @param actingPlayerIndex Index of the player who starts the acting
     * @param isPreFlop         true if the betting round is pre flop, false if not
     * @return True if the hand continues past this betting round, false if everyone but 1 folded
     */
    private boolean getDecisions(int actingPlayerIndex, boolean isPreFlop) {
        while (true) {
            //Determine who's turn it is
            actingPlayerIndex %= playersStillInCurrentHand.size();
            Player playerToAct = playersStillInCurrentHand.get(actingPlayerIndex);

            //Check if player is already all in
            if (playerToAct.isAllIn()) {
                if (allPlayersActed()) return true;
                actingPlayerIndex++;
                continue;
            }

            //Get decision for the acting player
            Decision decision = getValidDecisionFromPlayer(playerToAct, isPreFlop);
            playerToAct.act(decision, highestAmountPutOnTable, pot, isPreFlop);

            //Tell all the clients about this decision
            gameController.setDecisionForClient(playerToAct.getID(), decision);

            //Update players left in hand and number of players that have acted since the last aggressor
            switch (decision.move) {
                case BET:
                    highestAmountPutOnTable = decision.getSize();
                    break;
                case RAISE:
                    highestAmountPutOnTable += decision.getSize();
                    currentMinimumRaise = decision.getSize();
                    break;
                case ALL_IN:
                    if (playerToAct.putOnTable() >= highestAmountPutOnTable + currentMinimumRaise) {
                        currentMinimumRaise = playerToAct.putOnTable() - highestAmountPutOnTable;
                        highestAmountPutOnTable = playerToAct.putOnTable();
                    } else if (playerToAct.putOnTable() > highestAmountPutOnTable) {
                        highestAmountPutOnTable = playerToAct.putOnTable();
                    }
                    break;
                case FOLD:
                    playersStillInCurrentHand.remove(playerToAct);
                    break;
            }

            logger.println(playerToAct.getName() + " acted: " + decision + ", stack size = " + playerToAct.getStackSize(), Logger.MessageType.GAMEPLAY);

            //Check if the hand is over (only one player left)
            if (playersStillInCurrentHand.size() <= 1)
                return false;
            else if (allPlayersActed())
                return true;

            //If player folded actingPlayerIndex should not be incremented because playersInHand.size() is decremented
            if (decision.move != Decision.Move.FOLD)
                actingPlayerIndex++;
        }
    }

    /**
     * Gets a valid decision from a player, and checks if it is valid. The decision is returned if the move is valid.
     *
     * @param playerToAct Player to get decision from
     * @return Player's valid decision
     */
    private Decision getValidDecisionFromPlayer(Player playerToAct, boolean isPreFlop) {
        long stackSize = playerToAct.getStackSize();
        boolean playerCanCheckBigBlind =
                isPreFlop && playerToAct.putOnTable() == gameSettings.getBigBlind() && highestAmountPutOnTable == gameSettings.getBigBlind();

        //Get a decision for playerToAct from GameController
        Decision decision = gameController.getDecisionFromClient(playerToAct.getID());

        logger.replayLogPrint("\n" + playerToAct.getID() + " " + decision);

        //Test if decision is valid
        switch (decision.move) {
            case FOLD:
            case ALL_IN:
                return decision;

            case CHECK:
                if (highestAmountPutOnTable == 0)
                    return decision;

                if (playerCanCheckBigBlind) {
                    return decision;
                }
                break;

            case CALL:
                assert highestAmountPutOnTable >= 0 : playerToAct.getName() + " tried to call when amount put on table was " + highestAmountPutOnTable;
                if (highestAmountPutOnTable < stackSize + playerToAct.putOnTable())
                    return decision;
                else {
                    logger.print("Got decision call but highest amount was " + highestAmountPutOnTable +
                            ", stack size was " + stackSize + " and put on table was " + playerToAct.putOnTable() +
                            ". Returned decision all in instead", Logger.MessageType.DEBUG);
                    return new Decision(Decision.Move.ALL_IN);
                }

            case BET:
                if (highestAmountPutOnTable == 0)
                    return decision;
                break;

            case RAISE:
                assert highestAmountPutOnTable > 0 : playerToAct.getName() + " tried to raise by " + decision.getSize() + " when highest amount put on table was 0";
                if ((decision.getSize() + highestAmountPutOnTable - playerToAct.putOnTable()) > stackSize) {
                    logger.println(playerToAct.getName() + " tried to raise to " + (decision.getSize() + highestAmountPutOnTable) + " but only has " + stackSize, Logger.MessageType.GAMEPLAY);
                    break;
                }
                if (decision.getSize() >= currentMinimumRaise)
                    return decision;
                break;
            default:
                logger.println("Unknown move: " + decision.move, Logger.MessageType.DEBUG, Logger.MessageType.GAMEPLAY, Logger.MessageType.WARNINGS);
        }

        logger.println("**Invalid decision from " + playerToAct.getName() + ": " + decision + " - Return dummy decision**", Logger.MessageType.WARNINGS, Logger.MessageType.GAMEPLAY, Logger.MessageType.DEBUG);

        //Temp hack for testing
        if (highestAmountPutOnTable > 0)
            return new Decision(Decision.Move.CALL);
        else
            return new Decision(Decision.Move.CHECK);
    }

    /**
     * Automatically post small and big blind for given players
     */
    private void postBlinds() {
        assert playersStillInCurrentHand.size() >= 2 : "Not enough players still playing to post blinds";

        Decision postSB = new Decision(Decision.Move.SMALL_BLIND);
        Decision postBB = new Decision(Decision.Move.BIG_BLIND);

        Player smallBlindPlayer = playersStillInCurrentHand.get(0);
        Player bigBlindPlayer = playersStillInCurrentHand.get(1);

        //If one of the players don't have enough to post their blind
        if (smallBlindPlayer.getStackSize() <= gameSettings.getSmallBlind())
            postSB = new Decision(Decision.Move.ALL_IN);
        if (bigBlindPlayer.getStackSize() <= gameSettings.getBigBlind())
            postBB = new Decision(Decision.Move.ALL_IN);

        //Notify GUI and AI about the posting
        gameController.setDecisionForClient(smallBlindPlayer.getID(), postSB);
        gameController.setDecisionForClient(bigBlindPlayer.getID(), postBB);

        //Make players act
        smallBlindPlayer.act(postSB, 0, pot, true);
        bigBlindPlayer.act(postBB, 0, pot, true);

        stackSizes.put(smallBlindPlayer.getID(), smallBlindPlayer.getStackSize());
        stackSizes.put(bigBlindPlayer.getID(), bigBlindPlayer.getStackSize());

        currentMinimumRaise = gameSettings.getBigBlind();
        highestAmountPutOnTable = gameSettings.getBigBlind();
    }

    /**
     * Returns a list of the players who still have chips left (player.getStackSize() > 0).
     * Order: Small blind, big blind, UTG...
     * <p>
     * return Ordered list of players still in the game
     */
    private List<Player> getOrderedListOfPlayersStillPlaying() {
        //Reset the necessary maps
        positions = new HashMap<>();
        holeCards = new HashMap<>();

        //Update who is the small/big blind
        updateBlindIndexes();

        //Add players to orderedListOfPlayersStillPlaying in order SB, BB ...
        List<Player> orderedListOfPlayersStillPlaying = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            Player player = players[(smallBlindIndex + i) % numberOfPlayers];
            if (player.getStackSize() > 0) {
                orderedListOfPlayersStillPlaying.add(player);
            }
        }

        //Set position for all players and inform clients
        setNewPositions(orderedListOfPlayersStillPlaying);

        roundNumber++;
        return orderedListOfPlayersStillPlaying;
    }

    /**
     * Displays each player's hole cards after a hand is over, and gives the currentPot to the winning player.
     */
    private void showdown() {
        //Print all show down information to debugger
        printShowdownToDebugger();
        displayHoleCards();

        ShowdownStats showdownStats = new ShowdownStats(playersStillInCurrentHand, Arrays.asList(communityCards));
        pot.handOutPot(playersStillInCurrentHand, Arrays.asList(communityCards), showdownStats);
        assert pot.getPotSize() == 0 : "The pot was handed out, but there were still chips left";

        gameController.showdown(showdownStats);
        delay(HAND_OVER_DELAY);

        //If a player that was in this hand now has zero chips, it means he just busted
        playersStillInCurrentHand.stream().filter(p -> p.getStackSize() == 0).forEach(p -> {
            p.bustPlayer();
            gameController.bustClient(p.getID(), finishedInPosition);
            rankingTable.put(p.getID(), finishedInPosition);
            finishedInPosition--;
            remainingPlayers--;
        });
    }

    /**
     * Called if the hand is over before showdown (means everyone but one player folded).
     * Increments the stack of the player left in the hand. Sends relevant information to clients
     */
    private void preShowdownWinner() {
        assert playersStillInCurrentHand.size() == 1 : playersStillInCurrentHand.size() + " player left in hand";

        //Hand out the pot to the remaining player in the hand
        Player winner = playersStillInCurrentHand.get(0);
        gameController.preShowdownWinner(winner.getID(), winner.showCards);
        winner.handWon(winner.getHand(Arrays.asList(communityCards)), pot.getPotSize());
        logger.println("\n" + winner.getName() + " won the hand", Logger.MessageType.GAMEPLAY);
        pot = new Pot(logger);
        delay(EVERYONE_FOLDED_DELAY);
    }

    /**
     * Called when the game has ended (only one player left with chips)
     * Tells GameController to pass game statistics to GUIClient(s)
     */
    private void gameOver() {
        //Find the winning player
        Optional<Player> winner = Arrays.stream(players).filter(p -> p.getStackSize() > 0).findAny();
        assert winner.isPresent() : "No winner was determined when game was over";

        //add winner to ranking table
        rankingTable.put(winner.get().getID(), 1);

        //Create a new statistics for each player and send it
        Arrays.stream(players).forEach(p -> gameController.gameOver(p.getID(), new Statistics(p, names, rankingTable)));
    }

    /**
     * Display hole cards of remaining players
     */
    private void displayHoleCards() {
        // Map containing the hole cards of only the remaining players
        Map<Integer, Card[]> showdownHoleCards = new HashMap<>();
        for (Player p : playersStillInCurrentHand)
            showdownHoleCards.put(p.getID(), p.getHoleCards().clone());
        gameController.showHoleCards(showdownHoleCards);
    }

    /**
     * Used to check if a betting round should be skipped (all (or all but one) players are all in)
     *
     * @return True if betting round should be skipped
     */
    private boolean skipBettingRound() {
        for (Player p : playersStillInCurrentHand)
            if (!p.isAllIn() && p.putOnTable() < highestAmountPutOnTable)
                return false;

        return playersStillInCurrentHand.stream()
                .filter(Player::isAllIn)
                .count() >= playersStillInCurrentHand.size() - 1;
    }

    /**
     * Check if all the players have acted in this betting round
     * A player is finished acting if he is all in or he matches the highest amount put on the table
     *
     * @return True if all players are done acting this betting round
     */
    private boolean allPlayersActed() {
        int count = 0;
        for (Player p : playersStillInCurrentHand) {
            if (p.isAllIn())
                count++;
            else if (p.hasActed() && p.putOnTable() == highestAmountPutOnTable)
                count++;
        }

        return count == playersStillInCurrentHand.size();
    }

    /**
     * @return The total number of players with chips left (in the game, not the hand)
     */
    public int numberOfPlayersWithChipsLeft() {
        assert players != null : "List of players was null";
        int count = 0;
        for (int i = 0; i < players.length; i++) {
            assert players[i] != null : "Player " + i + " in players was null";
            if (players[i].getStackSize() > 0)
                count++;

        }
        return count;
    }

    /**
     * Method to set new positions on the table
     *
     * @param orderedPlayerList Ordered list of the players still playing (stack > 0)
     */
    private void setNewPositions(List<Player> orderedPlayerList) {
        //Determine positions on the table based on the order of playersStillInCurrentHand
        if (orderedPlayerList.size() == 2) {
            //Special case if only two players (dealer and small blind is same pos)
            positions.put(orderedPlayerList.get(0).getID(), 0);
            positions.put(orderedPlayerList.get(1).getID(), 1);
        } else {
            for (int i = 0; i < orderedPlayerList.size(); i++) {
                positions.put(orderedPlayerList.get(i).getID(), i);
            }
        }

        //Inform all clients about the updated positions
        gameController.setPositions(new HashMap<>(positions));
    }

    /**
     * Refresh the stackSizes-map and tell all clients the new stack sizes (sent at the beginning of each hand)
     */
    public void refreshAllStackSizes() {
        long totalChipsInPlay = 0;
        HashMap<Integer, Long> stacks = new HashMap<>();

        for (Player p : players) {
            if (!p.isBust()) {
                stacks.put(p.getID(), p.getStackSize());
                totalChipsInPlay += p.getStackSize();
            }
        }

        gameController.setStackSizes(stacks);

        totalChipsInPlay += pot.getPotSize();
        assert totalChipsInPlay == gameSettings.getMaxNumberOfPlayers() * gameSettings.getStartStack() : "Too many chips in play, " + totalChipsInPlay;
    }

    /**
     * Updates the small/big blind index. Called at the start of every hand
     */
    private void updateBlindIndexes() {
        //SmallBlindIndex skips players who are bust
        for (int i = 0; i < numberOfPlayers; i++) {
            if (players[(smallBlindIndex + i) % numberOfPlayers].getStackSize() <= 0)
                roundNumber++;
            else
                break;
        }

        //Set indexes
        smallBlindIndex = roundNumber % numberOfPlayers;
    }

    /**
     * Randomly generates and returns five community cards from the deck.
     *
     * @return Array of community cards
     */
    private Card[] generateCommunityCards() {
        logger.replayLogPrint("\nCARD");
        Card[] commCards = new Card[5];
        for (int i = 0; i < commCards.length; i++)
            commCards[i] = drawCard();
        return commCards;
    }

    /**
     * @return A random card from the deck if replay is false, the next card from the replay queue if not
     */
    private Card drawCard() {
        if (!(cardQueue == null) && cardQueue.isEmpty()) {
            delay(3000);
            System.exit(0);
        }

        Card draw = replay ? cardQueue.pop() : deck.draw().get();

        logger.replayLogPrint("\n" + draw.toString());
        return draw;
    }

    /**
     * Tells the game controller to display the flop.
     */
    private void setFlop() {
        gameController.setFlop(communityCards[0], communityCards[1], communityCards[2]);
    }

    /**
     * Tells the game controller to display the turn.
     */
    private void setTurn() {
        gameController.setTurn(communityCards[3]);
    }

    /**
     * Tells the game controller to display the river.
     */
    private void setRiver() {
        gameController.setRiver(communityCards[4]);
    }

    /**
     * Deals hole cards to each player still in the game.
     *
     * @param playersStillPlaying Players still in the game
     */
    private void dealHoleCards(List<Player> playersStillPlaying) {
        logger.replayLogPrint("\nCARD");
        for (Player p : playersStillPlaying) {
            Card[] cards = {drawCard(), drawCard()};
            p.setHoleCards(cards[0], cards[1]);
            holeCards.put(p.getID(), cards);
            gameController.setHandForClient(p.getID(), cards[0], cards[1]);
        }
    }

    /**
     * Prints all the information about the showdown to the debugger (and console)
     */
    private void printShowdownToDebugger() {
        //Print hole cards and community cards
        logger.println("\nShowdown", Logger.MessageType.GAMEPLAY);
        for (Player p : playersStillInCurrentHand)
            logger.println(p.getName() + " " + p.getHoleCards()[0] + p.getHoleCards()[1], Logger.MessageType.GAMEPLAY);
        for (Card communityCard : communityCards)
            logger.println(communityCard + " ", Logger.MessageType.GAMEPLAY);
        logger.println("");
    }

    /**
     * Print the current stack size of each player and current pot size (to debugLog and console)
     */
    public void printAllPlayerStacks() {
        for (Player p : players) {
            logger.println(p.getName() + "'s stack: " + p.getStackSize() + ", " + (positions.containsKey(p.getID()) ? GameScreen.getPositionName(positions.get(p.getID()), positions.size()) : "Bust"), Logger.MessageType.GAMEPLAY);
        }
        logger.println("Pot: " + pot.getPotSize(), Logger.MessageType.GAMEPLAY);
    }

    /**
     * Pauses the thread for a given amount of time.
     *
     * @param milliseconds Delay length
     */
    private void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            logger.println("Thread " + Thread.currentThread() + " was interrupted.", Logger.MessageType.WARNINGS, Logger.MessageType.DEBUG);
        }
    }

    /**
     * Get the current time
     * @return If this is a replay, this method will return the replayed games time, else it returns the current time in millis
     */
    private long getTime() {
        if (replay)
            if (replayTimeQueue.isPresent() && !replayTimeQueue.get().isEmpty())
                return this.replayTimeQueue.get().poll();
            else
                throw new NoSuchElementException("Replay time queue not present or empty");
        else
            return System.currentTimeMillis();
    }

    /**
     * Set the replay card queue
     * Only used if the game is a replay. Use this queue instead of deck.draw()
     *
     * @param cardQueue The queue of cards from the replay file
     */
    public void setReplayQueues(ArrayDeque<Card> cardQueue, ArrayDeque<Long> replayTimeQueue) {
        this.cardQueue = cardQueue;
        this.replayTimeQueue = Optional.of(replayTimeQueue);
        this.replay = true;
    }

    public static class InvalidGameSettingsException extends Exception {
        public InvalidGameSettingsException(String message) {
            super(message);
        }
    }
}
