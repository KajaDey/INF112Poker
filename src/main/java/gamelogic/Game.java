package gamelogic;

import gui.GUIMain;
import gui.GameSettings;

import java.util.*;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Game {

    //Controls
    private Player [] players;
    private GameController gameController;
    private GameSettings gamesettings;

    //Settings
    private int maxNumberOfPlayers;
    private int numberOfPlayers = 0, finishedInPosition;
    private int blindLevelDuration;
    private long startSB, startBB;
    private long currentSB, currentBB;
    private long startStack;

    //Indexes
    private int smallBlindIndex = 0;

    //Round specific
    private Pot pot = new Pot();
    private int roundNumber = 0;
    private long highestAmountPutOnTable = 0, currentMinimumRaise = 0;
    private Map<Integer, Long> stackSizes;
    private List<Player> playersStillInCurrentHand;
    private Map<Integer, Card[]> holeCards;
    private Map<Integer, Integer> positions;
    private Card [] communityCards;

    public Game(GameSettings gamesettings, GameController gameController) {
        this.gameController = gameController;
        this.gamesettings = gamesettings;

        this.maxNumberOfPlayers = finishedInPosition =  gamesettings.getMaxNumberOfPlayers();
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.currentSB = (this.startSB = gamesettings.smallBlind);
        this.currentBB = (this.startBB = gamesettings.bigBlind);
        this.blindLevelDuration = gamesettings.levelDuration;
        this.stackSizes = new HashMap<>();
    }

    /**
     * Plays a game until a player has won.
     *
     */
    public void playGame() {

        Gameloop:
        while(numberOfPlayersWithChipsLeft() > 1) {
            GUIMain.debugPrintln("\nNew hand");
            //Tell all clients that a new hand has started and update all players stack sizes
            gameController.startNewHand();
            pot = new Pot();
            refreshAllStackSizes();

            //Get an ordered list of players in the current hand (order BTN, SB, BB...)
            playersStillInCurrentHand = getOrderedListOfPlayersStillPlaying();

            //Deal all hole cards and save community cards for later use
            Deck deck = new Deck();
            communityCards = generateCommunityCards(deck);
            dealHoleCards(deck, playersStillInCurrentHand);

            playHand();
        }

        //Deal with who won the game.. (should be the only player with chips left
        assert numberOfPlayersWithChipsLeft() == 1 : "Game over but " + numberOfPlayersWithChipsLeft() + " had chips left";
        pot = new Pot();
        refreshAllStackSizes();

        for (Player p : players) {
            if (p.getStackSize() > 0) {
                gameController.gameOver(p.getID());
                return;
            }
        }
    }

    private void playHand() {
        boolean preFlop = true;

        //Makes the small and big blind pay their blind by forcing an act. Updates stackSizes
        GUIMain.debugPrintln("\nBLINDS");
        postBlinds();
        printAllPlayerStacks();

        //First betting round (preflop)
        GUIMain.debugPrintln("\nPREFLOP:");

        boolean handContinues = bettingRound(preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display flop and new betting round
        GUIMain.debugPrintln("\nFLOP:");
        setFlop();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display turn and new betting round
        GUIMain.debugPrintln("\nTURN:");
        setTurn();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Display river and new betting round
        GUIMain.debugPrintln("\nRIVER:");
        setRiver();
        handContinues = bettingRound(!preFlop);
        if (!handContinues) {
            preShowdownWinner();
            return;
        }

        //Showdown
        showDown();
    }

    /**
     * Called if the hand is over before showdown (means everyone but one player folded).
     * Increments the stack of the player left in the hand. Sends relevant information to clients
     */
    private void preShowdownWinner() {
        assert playersStillInCurrentHand.size() == 1 : playersStillInCurrentHand.size() + " player left in hand";

        //Hand out the pot to the remaining player in the hand
        Player winner = playersStillInCurrentHand.get(0);
        gameController.preShowdownWinner(winner.getID(), pot.getPotSize());
        winner.incrementStack(pot.getPotSize());
        delay(3000);
    }

    /**
     * Used to check if a betting round should be skipped (all (or all but one) players are all in)
     *
     *  @return True if betting round should be skipped, false if not
     */
    private boolean skipBettingRound() {
        int count = 0;
        for (Player p : playersStillInCurrentHand)
            if (p.isAllIn())
                count++;

        return count >= playersStillInCurrentHand.size() - 1;
    }

    /**
     *   Play one complete betting round (where all players act until everyone agrees (or everyone but 1 folds))
     * @param isPreFlop  True if this is the pre flop betting round
     * @return  True if the hand continues, false if the hand is over
     */
    private boolean bettingRound(boolean isPreFlop) {
        //Determine who is acting first (based on the isPreFLop-value)
        int actingPlayerIndex;

        if (!isPreFlop && numberOfPlayers == 2)
            actingPlayerIndex = 1;
        else
            actingPlayerIndex = (isPreFlop ? 2 % playersStillInCurrentHand.size() : 0);

        highestAmountPutOnTable = (isPreFlop ? currentBB : 0);
        currentMinimumRaise = currentBB;

        if (!isPreFlop) {
            for (Player p : players)
                p.newBettingRound();
        }

        //Check if all players are all in and betting round should be skipped
        if (skipBettingRound()) {
            displayHoleCards();
            delay(2000);
            return true;
        }

        //Return true if hand continues, false if hand is over
        return getDecisions(actingPlayerIndex, isPreFlop);
    }

    /**
     *   Get decision from all players until all agressive moves have been responded to
     * @param actingPlayerIndex  Index of the player who starts the acting
     * @param isPreFlop  true if the betting round is pre flop, false if not
     * @return  True if the hand continues past this betting round, false if everyone but 1 folded
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
            String name = playerToAct.getName();
            switch(decision.move) {
                case BET:
                    highestAmountPutOnTable = decision.size;
                    break;
                case RAISE:
                    highestAmountPutOnTable += decision.size;
                    currentMinimumRaise = decision.size;
                    break;
                case ALL_IN:
                    if(playerToAct.getAmountPutOnTableThisBettingRound() >= highestAmountPutOnTable+currentMinimumRaise) {
                        currentMinimumRaise = playerToAct.getAmountPutOnTableThisBettingRound() - highestAmountPutOnTable;
                        highestAmountPutOnTable = playerToAct.getAmountPutOnTableThisBettingRound();
                    } else if (playerToAct.getAmountPutOnTableThisBettingRound() > highestAmountPutOnTable){
                        highestAmountPutOnTable = playerToAct.getAmountPutOnTableThisBettingRound();
                    }
                    break;
                case FOLD:
                    playersStillInCurrentHand.remove(playerToAct);
                    break;
            }

            GUIMain.debugPrintln(playerToAct.getName()  + " acted: " + decision);

            //Check if the hand is over (only one player left)
            if (playersStillInCurrentHand.size() <= 1)
                return false;
            else if(allPlayersActed())
                return true;

            //If player folded actingPlayerIndex should not be incremented because playersInHand.size() is decremented
            if (decision.move != Decision.Move.FOLD)
                actingPlayerIndex++;
        }
    }

    /**
     * Display hole cards of remaining players
     */
    private void displayHoleCards() {
        ArrayList<Integer> playerList = new ArrayList<>();
        for (Player p : playersStillInCurrentHand)
            playerList.add(p.getID());

        gameController.showHoleCards(playerList, holeCards);
    }

    /**
     *   Check if all the players have acted in this betting round
     *   A player is finished acting if he is all in or he matches the highest amount put on the table
     * @return
     */
    private boolean allPlayersActed() {
        int count = 0;
        for (Player p : playersStillInCurrentHand) {
            if (p.isAllIn())
                count++;
            else if (p.hasActed() && p.getAmountPutOnTableThisBettingRound() == highestAmountPutOnTable)
                count++;
        }

        return count == playersStillInCurrentHand.size();
    }

    /**
     * Automatically post small and big blind for given players
     */
    private void postBlinds() {
        assert playersStillInCurrentHand.size() >= 2 : "Not enough players still playing to post blinds";

        Decision postSB = new Decision(Decision.Move.SMALL_BLIND, currentSB);
        Decision postBB = new Decision(Decision.Move.BIG_BLIND, currentBB);

        Player smallBlindPlayer, bigBlindPlayer;
        if (playersStillInCurrentHand.size() == 2) {
            smallBlindPlayer = playersStillInCurrentHand.get(0);
            bigBlindPlayer = playersStillInCurrentHand.get(1);
        } else {
            smallBlindPlayer = playersStillInCurrentHand.get(0);
            bigBlindPlayer = playersStillInCurrentHand.get(1);
        }

        //If one of the players don't have enough to post their blind
        if (smallBlindPlayer.getStackSize() <= postSB.size)
            postSB = new Decision(Decision.Move.ALL_IN);
        if (bigBlindPlayer.getStackSize() <= postBB.size)
            postBB = new Decision(Decision.Move.ALL_IN);

        //Notify GUI and AI about the posting
        gameController.setDecisionForClient(smallBlindPlayer.getID(), postSB);
        gameController.setDecisionForClient(bigBlindPlayer.getID(), postBB);

        //Make players act
        smallBlindPlayer.act(postSB, 0, pot, true);
        bigBlindPlayer.act(postBB, 0, pot, true);

        stackSizes.put(smallBlindPlayer.getID(), smallBlindPlayer.getStackSize());
        stackSizes.put(bigBlindPlayer.getID(), bigBlindPlayer.getStackSize());

        currentMinimumRaise = currentBB;
        highestAmountPutOnTable = currentBB;
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
                isPreFlop && playerToAct.getAmountPutOnTableThisBettingRound() == currentBB && highestAmountPutOnTable == currentBB;

        while(true) {
            //Get a decision for playerToAct from GameController
            Decision decision = gameController.getDecisionFromClient(playerToAct.getID());

            //Test if decision is valid
            switch(decision.move) {
                case FOLD: case ALL_IN:
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
                    if (highestAmountPutOnTable < stackSize)
                        return decision;
                    else
                        return new Decision(Decision.Move.ALL_IN);

                case BET:
                    if (highestAmountPutOnTable == 0)
                        return decision;
                    break;

                case RAISE:
                    assert highestAmountPutOnTable > 0 : playerToAct.getName() + " tried to raise by " + decision.size + " when highest amount put on table was 0";
                    if (decision.size >= currentMinimumRaise)
                        return decision;
                    break;
                default: GUIMain.debugPrintln("Unknown move: " + decision.move);
            }

            GUIMain.debugPrintln("**Invalid decision from " + playerToAct.getName() + ": " + decision + " - Return dummy decision**");

            //Temp hack for testing
            if (highestAmountPutOnTable > 0)
                return new Decision(Decision.Move.CALL);
            else
                return new Decision(Decision.Move.CHECK);
        }
    }

    /**
     * Returns a list of the players who still have chips left (player.getStackSize() > 0).
     * Order: Small blind, big blind, etc...
     *
     * return Ordered list of players still in the game
     */
    private List<Player> getOrderedListOfPlayersStillPlaying() {
        //Reset the necessary maps
        positions = new HashMap<>();
        holeCards = new HashMap<>();

        //Update who is the small/big blind
        updateBlindIndexes();

        //Add players to orderedListOfPlayersStillPlaying in order SB, BB ...
        List<Player> orderedListOfPlayersStillPlaying = new ArrayList<Player>();
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
     *  Method to set new positions on the table
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

        //Inform all clients about the udpates positions
        gameController.setPositions(new HashMap<>(positions));
    }

    /**
     *  Updates the small/big blind index. Called at the start of every hand
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
     * Adds a new player to the game. The player is given a start stack size.
     *
     * @param name Name of the player
     * @param ID Player-ID
     * @return true if player was added successfully, else false
     */
    public boolean addPlayer(String name, int ID) {
        if (numberOfPlayers >= maxNumberOfPlayers) {
            return false;
        }

        Player p = new Player(name, startStack, ID);
        for (int i = 0; i < maxNumberOfPlayers; i++) {
            if (players[i] == null) {
                players[i] = p;
                numberOfPlayers++;
                break;
            }
        }

        stackSizes.put(ID, gamesettings.getStartStack());

        return true;
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
            GUIMain.debugPrintln("Thread " + Thread.currentThread() + " was interrupted.");
        }
    }

    /**
     * Tells the game controller to display the flop.
     */
    private void setFlop() {
        gameController.setFlop(communityCards[0], communityCards[1], communityCards[2], pot.getPotSize());
    }

    /**
     * Tells the game controller to display the turn.
     */
    private void setTurn() {
        gameController.setTurn(communityCards[3], pot.getPotSize());
    }

    /**
     * Tells the game controller to display the river.
     */
    private void setRiver() {
        gameController.setRiver(communityCards[4], pot.getPotSize());
    }

    /**
     * Deals hole cards to each player still in the game.
     *
     * @param deck Deck to draw from
     * @param playersStillPlaying Players still in the game
     */
    private void dealHoleCards(Deck deck, List<Player> playersStillPlaying) {
        for (Player p : playersStillPlaying) {
            Card[] cards = {deck.draw().get(), deck.draw().get()};
            p.setHoleCards(cards[0], cards[1]);
            holeCards.put(p.getID(), cards);
            gameController.setHandForClient(p.getID(), cards[0], cards[1]);
        }
    }

    /**
     *  Checks for errors in the game settings
     *  @return The appropriate error message if there is an error, null otherwise
     */
    public String getError() {
        String error = null;
        if (startStack < 0) {
            error = "Start stack must be a positive whole number";
        } else if (startStack < startBB * 10){
            error = "Start stack must be at least 10 times the big blind";
        } else if(startBB < 0 || startSB < 0) {
            error = "All blinds must be positive whole numbers";
        } else if (startBB < startSB * 2) {
            error = "Big blind must be at least twice the size of the small blind";
        } else if(maxNumberOfPlayers < 2 || maxNumberOfPlayers > 8) {
            error = "Max number of players must be between 2-8";
        } else if(blindLevelDuration <= 0) {
            error = "Blind level must be a positive whole number";
        }

        return error;
    }

    /**
     * Randomly generates and returns five community cards from the deck.
     * @param deck Deck to draw from
     * @return Array of community cards
     */
    private Card[] generateCommunityCards(Deck deck) {
        Card[] commCards = new Card[5];
        for (int i = 0; i < commCards.length; i++)
            commCards[i] = deck.draw().get();
        return commCards;
    }

    /**
     * Displays each player's hole cards after a hand is over, and gives the currentPot to the winning player.
     */
    private void showDown() {
        //Print all show down information to debugger
        printToDebugShowdown();

        ShowdownStats showdownStats = new ShowdownStats(playersStillInCurrentHand, Arrays.asList(communityCards));
        pot.handOutPot(playersStillInCurrentHand, Arrays.asList(communityCards), showdownStats);
        assert pot.getPotSize() == 0 : "The pot was handed out, but there was still chips left";

        gameController.showDown(showdownStats);
        delay(7000);

        //If a player that was in this hand now has zero chips, it means he just busted
        for (Player p : playersStillInCurrentHand) {
            if (p.getStackSize() == 0)
                 gameController.bustClient(p.getID(), finishedInPosition--);
        }
    }

    /**
     * Prints all the information about the showdown to the debugger (and console)
     */
    private void printToDebugShowdown() {
        //Print hole cards and community cards
        GUIMain.debugPrintln("\nShowdown");
        for (Player p : playersStillInCurrentHand)
            GUIMain.debugPrintln(p.getName() + " " + p.getHoleCards()[0] + p.getHoleCards()[1]);
        for (Card communityCard : communityCards)
            GUIMain.debugPrint(communityCard + " ");
        GUIMain.debugPrintln();
    }

    public void refreshAllStackSizes() {
        long totalChipsInPlay = 0;
        HashMap<Integer, Long> stacks = new HashMap<>();

        for (Player p : players)
            if (p.getStackSize() > 0) {
                stacks.put(p.getID(), p.getStackSize());
                totalChipsInPlay += p.getStackSize();
            }

        gameController.setStackSizes(stacks);

        totalChipsInPlay += pot.getPotSize();
        assert totalChipsInPlay == maxNumberOfPlayers * gamesettings.getStartStack() : "Too many chips in play, " + totalChipsInPlay;
    }

    public void printAllPlayerStacks() {
        for (Player p : players) {
            GUIMain.debugPrintln(p.getName() + "'s stack: " + p.getStackSize());
        }
        GUIMain.debugPrintln("Pot: " + pot.getPotSize());
    }

    public int numberOfPlayersWithChipsLeft(){
        int count = 0;
        for (Player p : players) {
            if (p.getStackSize() > 0)
                count++;
        }
        return count;
    }

}
