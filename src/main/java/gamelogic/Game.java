package gamelogic;

import gui.GameSettings;

import java.util.*;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Game {

    //Controls
    private Table table;
    private Player [] players;
    private GameController gameController;
    private GameSettings gamesettings;

    //Settings
    private int maxNumberOfPlayers;
    private int numberOfPlayers = 0;
    private int blindLevelDuration;
    private long startSB, startBB;
    private long currentSB, currentBB;
    private long startStack;

    //Indexes
    private int dealerIndex = 0;
    private int bigBlindIndex = 0;
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

        this.maxNumberOfPlayers = gamesettings.getMaxNumberOfPlayers();
        this.table = new Table(maxNumberOfPlayers);
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

        while(numberOfPlayersWithChipsLeft() > 1) {
            System.out.println("\nNew hand");
            //Tell all clients that a new hand has started and update all players stacksizes
            gameController.startNewHand();
            refreshAllStackSizes();
            pot = new Pot();

            //Get an ordered list of players in the current hand (order BTN, SB, BB...)
            playersStillInCurrentHand = getOrderedListOfPlayersStillPlaying();
            gameController.setPositions(new HashMap<>(positions));

            //Deal all hole cards and save community cards for later use
            Deck deck = new Deck();
            communityCards = getCommunityCards(deck);
            dealHoleCards(deck, playersStillInCurrentHand);

            printAllPlayerStacks();

            //Makes the small and big blind pay their blind by forcing an act. Updates stackSizes
            System.out.println("\nBLINDS");
            postBlinds();
            printAllPlayerStacks();

            //First betting round (preflop)
            System.out.println("\nPREFLOP:");
            boolean handContinues = bettingRound(true);
            if (!handContinues) { determineWinner(false);  continue; }
            printAllPlayerStacks();

            //Display flop and new betting round
            System.out.println("\nFLOP:");
            setFlop();
            handContinues = bettingRound(false);
            if (!handContinues) { determineWinner(false); continue; }
            printAllPlayerStacks();

            //Display turn and new betting round
            System.out.println("\nTURN:");
            setTurn();
            handContinues = bettingRound(false);
            if (!handContinues) { determineWinner(false); continue;}
            printAllPlayerStacks();

            //Display river and new betting round
            System.out.println("\nRIVER:");
            setRiver();
            handContinues = bettingRound(false);
            printAllPlayerStacks();

            //Showdown
            determineWinner(true);
        }

        //Deal with who won the game.. (should be the only player with chips left
        assert numberOfPlayersWithChipsLeft() == 1 : "Game over but " + numberOfPlayersWithChipsLeft() + " had chips left";
        refreshAllStackSizes();

        for (Player p : players) {
            if (p.getStackSize() > 0) {
                gameController.gameOver(p.getID());
                return;
            }
        }
    }

    private void determineWinner(boolean isShowDown) {
        //If all community cards are out and we have a showdown
        if (isShowDown) {
            showDown();
            return;
        }

        //Double check that there is only one player remaining in the hand
        if (playersStillInCurrentHand.size() != 1)
            System.out.println("Winner cannot be determined, " + playersStillInCurrentHand.size() + " players left in the hand");

        //Hand out the pot to the remaining player in the hand
        Player winner = playersStillInCurrentHand.get(0);
        winner.incrementStack(pot.getPotSize());
    }

    /**
     *  Used to find out if game has ended
     *  Returns the number of players that have getStackSize() > 0
     *  @return
     */
    private int numberOfPlayersWithChipsLeft() {
        int numberOfPlayersWithChipsLeft = 0;
        for (Player p : players) {
            if (p.getStackSize() > 0) numberOfPlayersWithChipsLeft++;
        }
        return numberOfPlayersWithChipsLeft;
    }

    /**
     * Runs one betting round until all players still in the hand have checked, or bet the same amount.
     * Returns false if there is only one player left in the hand (everyone else folded), else true
     *
     */
    private boolean bettingRound(boolean isPreFlop) {
        //Determine who is acting first (based on the isPreFLop-value)
        int actingPlayerIndex = (isPreFlop ? 0 : 1);
        highestAmountPutOnTable = (isPreFlop ? currentBB : 0);
        currentMinimumRaise = currentBB;

        if (!isPreFlop) {
            for (Player p : players)
                p.newBettingRound();
        }

        int numberOfPlayersActedSinceLastAggressor = 0;

        //Check if all players are all in and betting round should be skipped
        if (numberOfPlayersWithChipsLeft() <= 1) { return true; }

        while (true) {

            //Determine who's turn it is
            actingPlayerIndex %= playersStillInCurrentHand.size();
            Player playerToAct = playersStillInCurrentHand.get(actingPlayerIndex);

            //Check if player is already all in
            if (playerToAct.getStackSize() == 0) {
                if (numberOfPlayersWithChipsLeft() == 0)
                    return true;
                actingPlayerIndex++;
                continue;
            }

            //Get decision for the acting player
            Decision decision = getValidDecisionFromPlayer(playerToAct, isPreFlop);
            playerToAct.act(decision, highestAmountPutOnTable, pot);

            //Tell all the clients about this decision
            gameController.setDecisionForClient(playerToAct.getID(), decision);

            //Update players left in hand and number of players that have acted since the last aggressor
            switch(decision.move) {
                case BET:
                    numberOfPlayersActedSinceLastAggressor = 1;
                    highestAmountPutOnTable = decision.size;
                    break;
                case RAISE:
                    numberOfPlayersActedSinceLastAggressor = 1;
                    highestAmountPutOnTable += decision.size;
                    currentMinimumRaise = decision.size;
                    break;
                case ALL_IN:
                    if(playerToAct.getAmountPutOnTableThisBettingRound() >= highestAmountPutOnTable+currentMinimumRaise) {
                        numberOfPlayersActedSinceLastAggressor = 1; //If all in is a valid raise
                        currentMinimumRaise = playerToAct.getAmountPutOnTableThisBettingRound() - highestAmountPutOnTable;
                        highestAmountPutOnTable = playerToAct.getAmountPutOnTableThisBettingRound();
                    } else if (playerToAct.getAmountPutOnTableThisBettingRound() >= highestAmountPutOnTable){
                        numberOfPlayersActedSinceLastAggressor++; //If all in was not a valid raise but a raise
                        highestAmountPutOnTable = playerToAct.getAmountPutOnTableThisBettingRound();
                    } else {
                        numberOfPlayersActedSinceLastAggressor++;
                    }
                    break;
                case FOLD:
                    playersStillInCurrentHand.remove(playerToAct);
                    break;

                default: numberOfPlayersActedSinceLastAggressor++;
            }

            System.out.println(playerToAct.getName()  + " acted: " + decision + ". Highest amount put on table: " + highestAmountPutOnTable);

            //Check if the hand is over (only one player left)
            if (playersStillInCurrentHand.size() <= 1) {
                System.out.println("Only one player left, hand over");
                return false;
            } else if(numberOfPlayersActedSinceLastAggressor == playersStillInCurrentHand.size()) {
                return true;
            }

            actingPlayerIndex++;
        }
    }

    /**
     * Automatically post small and big blind for given players
     *
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
            smallBlindPlayer = playersStillInCurrentHand.get(1);
            bigBlindPlayer = playersStillInCurrentHand.get(2);
        }

        //Notify GUI and AI about the posting
        gameController.setDecisionForClient(smallBlindPlayer.getID(), postSB);
        gameController.setDecisionForClient(bigBlindPlayer.getID(), postBB);

        //Make players act
        smallBlindPlayer.act(postSB, 0, pot);
        bigBlindPlayer.act(postBB, 0, pot);

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

                    System.out.println(playerToAct.getAmountPutOnTableThisBettingRound() + " " + isPreFlop + " " + highestAmountPutOnTable);
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
                    assert highestAmountPutOnTable > 0 : playerToAct.getName() + " tried to raise when highest amount put on table was 0";
                    if (decision.size >= currentMinimumRaise)
                        return decision;
                    break;
                default: System.out.println("Unknown move: " + decision.move);
            }

            System.out.println("**Invalid decision from " + playerToAct.getName() + ": " + decision + " - Return dummy decision**");

            //Temp hack for testing
            if (highestAmountPutOnTable > 0)
                return new Decision(Decision.Move.CALL);
            else
                return new Decision(Decision.Move.CHECK);
        }
    }

    /**
     * Returns a list of the players who still have chips left (player.getStackSize() > 0).
     * Order: Dealer button, small blind, big blind, etc...
     *
     * return Ordered list of players still in the game
     */
    private List<Player> getOrderedListOfPlayersStillPlaying() {
        //Reset the necessary variables
        positions = new HashMap<>();
        holeCards = new HashMap<>();

        //Set indexes
        dealerIndex = roundNumber % numberOfPlayers;
        smallBlindIndex = (numberOfPlayers == 2 ? roundNumber % numberOfPlayers : (roundNumber+1)%numberOfPlayers);
        bigBlindIndex = (smallBlindIndex+1) % numberOfPlayers;

        List<Player> orderedListOfPlayersStillPlaying = new ArrayList<Player>();
        //Add players to orderedListOfPlayersStillPlaying in order BTN, SB, BB ...
        for (int i = 0; i < numberOfPlayers; i++) {
            Player player = players[(smallBlindIndex + i) % numberOfPlayers];
            if (player.getStackSize() > 0) {
                orderedListOfPlayersStillPlaying.add(player);
            }
        }


        //Determine positions on the table based on the order of playersStillInCurrentHand
        if (orderedListOfPlayersStillPlaying.size() == 2) {
            //Special case if only two players (dealer and small blind is same pos)
            positions.put(orderedListOfPlayersStillPlaying.get(0).getID(), 0);
            positions.put(orderedListOfPlayersStillPlaying.get(1).getID(), 2);
        } else {
            for (int i = 0; i < orderedListOfPlayersStillPlaying.size(); i++) {
                positions.put(orderedListOfPlayersStillPlaying.get(i).getID(), i);
            }
        }

        roundNumber++;
        return orderedListOfPlayersStillPlaying;
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
            System.out.println("Error when sleeping thread " + Thread.currentThread());
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
    private Card[] getCommunityCards(Deck deck) {
        Card[] commCards = new Card[5];
        for (int i = 0; i < commCards.length; i++)
            commCards[i] = deck.draw().get();
        return commCards;
    }

    /**
     * Calculates how many players are all in this round.
     *
     * @param playersStillPlaying Players still in the hand
     * @return Number of players all in
     */
    private int numberOfPlayersAllIn(List<Player> playersStillPlaying) {
        int numberOfPlayersAllIn = 0;

        for (Player p : playersStillPlaying) {
            assert p.getStackSize() >= 0 : p.getName() + "'s stack was " + p.getStackSize();
            if (p.getStackSize() - p.getAmountPutOnTableThisBettingRound() <= 0)
                numberOfPlayersAllIn++;
        }
        return numberOfPlayersAllIn;
    }

    /**
     * Finds ID of the player who won the hand
     *
     * @param playersStillPlaying Players still in the hand
     * @return Winner-ID
     */
    private int findWinnerID(List<Integer> playersStillPlaying) {
        int bestPlayer = playersStillPlaying.get(0);
        Hand bestHand = new Hand(holeCards.get(bestPlayer)[0], holeCards.get(bestPlayer)[1], Arrays.asList(communityCards));

        for (Integer i : playersStillPlaying) {
            Hand currentHand = new Hand(holeCards.get(i)[0], holeCards.get(i)[1], Arrays.asList(communityCards));

            if (currentHand.compareTo(bestHand) > 0) {
                bestHand = currentHand;
                bestPlayer = i;
            }
        }

        return bestPlayer;
    }

    /**
     * Displays each player's hole cards after a hand is over, and gives the currentPot to the winning player.
     */
    private void showDown() {
        List<Integer> IDStillPlaying = new ArrayList<>();
        for (Player p : playersStillInCurrentHand) {
            IDStillPlaying.add(p.getID());
        }

        //Winner of the main pot
        int winnerID = findWinnerID(IDStillPlaying);
        Player winner = getPlayerFromID(winnerID);
        long potShare = pot.getSharePotPlayerCanWin(winnerID);
        winner.incrementStack(potShare);
        gameController.showDown(IDStillPlaying, winnerID, holeCards, potShare);
        IDStillPlaying.remove(new Integer(winnerID));

        //While there are more side pots
        while (pot.getPotSize() > 0) {
            winnerID = findWinnerID(IDStillPlaying);
            winner = getPlayerFromID(winnerID);
            potShare = pot.getSharePotPlayerCanWin(winnerID);
            winner.incrementStack(potShare);
            System.out.println(winner.getName() + " got " + potShare);
            IDStillPlaying.remove(new Integer(winnerID));
        }

        delay(5000);
    }

    public void refreshAllStackSizes() {
        for (Player p : players)
            stackSizes.put(p.getID(), p.getStackSize());

        gameController.setStackSizes(stackSizes);
    }

    public void printAllPlayerStacks() {
        for (Player p : players) {
            System.out.println(p.getName() + "'s stack: " + p.getStackSize());
        }
        System.out.println("Pot: " + pot.getPotSize());
    }

    public Player getPlayerFromID(int ID) {
        for (Player p : players) {
            if (p.getID() == ID) return p;
        }
        return null;
    }

}
