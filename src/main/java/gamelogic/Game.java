package main.java.gamelogic;

import main.java.gui.GUIClient;
import main.java.gui.GameSettings;

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

    //Rounds
    private int roundNumber = 0;
    private long currentBet = 0L;
    private long biggestBet;
    private long pot = 0;
    private Map<Integer, Long> stackSizes;
    private Card [] communityCards;
    private Map<Integer, Card[]> holeCards;
    private List<Player> playersStillPlaying;

    public Game(GameSettings gamesettings, GameController gameController) {
        this.gameController = gameController;
        this.gamesettings = gamesettings;

        this.maxNumberOfPlayers = gamesettings.getMaxNumberOfPlayers();
        this.table = new Table(maxNumberOfPlayers);
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.startSB = gamesettings.smallBlind;
        this.startBB = gamesettings.bigBlind;
        this.blindLevelDuration = gamesettings.levelDuration;
        this.stackSizes = new HashMap<>();
    }

    /**
     * Plays a game until a player has won.
     *
     */
    public void playGame() {
        assert numberOfPlayers == maxNumberOfPlayers : "Incorrect number of players";

        //Initiate clients
        gameController.initClients(gamesettings);

        currentSB = startSB;
        currentBB = startBB;

        boolean remainingPlayers = true;

        Handloop:
        while (true) {
            gameController.setStackSizes(stackSizes);
            gameController.startNewHand();

            playersStillPlaying = new ArrayList<>();
            initializeNewHand(playersStillPlaying);

            if (playersStillPlaying.size() <= 1) {
                assert playersStillPlaying.size() != 0 : "Game ended, but no player won";
                gameController.gameOver(playersStillPlaying.get(0).getID());
                return;
            }

            //Generate cards
            Deck deck = new Deck();
            dealHoleCards(deck, playersStillPlaying);
            communityCards = generateCommunityCards(deck);

            currentBet = currentBB;
            biggestBet = currentBB;

            //Preflop
            postBlinds(playersStillPlaying, smallBlindIndex, bigBlindIndex, currentSB, currentBB);
            remainingPlayers = bettingRound(playersStillPlaying, 2, true);
            if (!remainingPlayers) { playersStillPlaying.get(0).incrementStack(pot); updateStackSizes(); continue; }

            //Flop
            setFlop();
            remainingPlayers = bettingRound(playersStillPlaying, 0, false);
            if (!remainingPlayers) { playersStillPlaying.get(0).incrementStack(pot); updateStackSizes(); continue; }

            //Turn
            setTurn();
            remainingPlayers = bettingRound(playersStillPlaying, 0, false);
            if (!remainingPlayers) { playersStillPlaying.get(0).incrementStack(pot); updateStackSizes(); continue; }

            //River
            setRiver();
            remainingPlayers = bettingRound(playersStillPlaying, 0, false);
            if (!remainingPlayers) { playersStillPlaying.get(0).incrementStack(pot); updateStackSizes(); continue; }

            //Showdown
            System.out.println("SHOWDOWN");
            if (playersStillPlaying.size() > 1) {
                for (Player player : playersStillPlaying) {
                    System.out.println("Player " + player.getID() + ": " + player.cardsOnHand());
                }
                System.out.println("Community cards: ");
                for (Card c : communityCards) {
                    System.out.print(c + " ");
                }
                System.out.println();
            }

            this.showDown();
        }
    }

    /**
     * Runs one betting round until all players still in the hand have checked, or bet the same amount.
     * Returns false if there is only one player left in the hand (everyone else folded), else true
     *
     * @param playersStillPlaying Players still in the hand
     * @param actingPlayerIndex Index of the acting player
     * @param isPreflop true if the hand is preflop, else false
     * @return false if the hand is over, else true
     */
    private boolean bettingRound(List<Player> playersStillPlaying, int actingPlayerIndex, boolean isPreflop) {
        gameController.setStackSizes(stackSizes);

        int numberOfActedPlayers = 0;
        if (!isPreflop) {
            currentBet = 0;
            biggestBet = 0;
        }

        while (true) {
            actingPlayerIndex %= numberOfPlayers;
            Player playerToAct = playersStillPlaying.get(actingPlayerIndex);

            //Check if the player is already all in
            if (playerToAct.getStackSize() - playerToAct.getAmountPutOnTableThisBettingRound() == 0) {
                if (numberOfPlayersAllIn(playersStillPlaying) >= playersStillPlaying.size() - 1) {
                    //Everyone (or everyone but 1 player) is all in
                    return true;
                } else {
                    //Player is all in, don't ask for decision
                    continue;
                }
            }

            Decision decision = getValidDecisionFromPlayer(playerToAct, isPreflop);
            playerToAct.act(decision, currentBet);

            //This changes to false when BB has acted for the first time preflop, to ensure that he can not check to a raise
            isPreflop = (isPreflop && (actingPlayerIndex == bigBlindIndex)) ? false : isPreflop;

            System.out.println(playerToAct.getName() + " acted: " + decision);
            gameController.setDecisionForClient(playerToAct.getID(), decision);

            switch(decision.move) {
                case RAISE:case BET:
                    numberOfActedPlayers = 1;
                    currentBet += decision.size;
                    assert decision.size >= biggestBet || playerToAct.getStackSize() - playerToAct.getAmountPutOnTableThisBettingRound() == 0;
                    biggestBet = Math.max(biggestBet, decision.size);
                    biggestBet = decision.size;
                    break;
                case FOLD: playersStillPlaying.remove(playerToAct); break;
                default: numberOfActedPlayers++;
            }

            //If only one player left in hand
            if (playersStillPlaying.size() <= 1) {
                System.out.println("Only one player left, hand over");
                updateStackSizes();
                updatePot();
                return false;
            }

            //If all players have acted
            if (numberOfActedPlayers == playersStillPlaying.size()) {
                updateStackSizes();
                updatePot();
                return true;
            }

            actingPlayerIndex++;
        }
    }

    /**
     * Automatically post small and big blind for given players
     *
     * @param playersStillPlaying Players still in the game
     * @param sbID ID of the player to post small blind
     * @param bbID ID of the player to post big blind
     * @param SB Small blind amount
     * @param BB Big blind amount
     */
    private void postBlinds(List<Player> playersStillPlaying, int sbID, int bbID, Long SB, Long BB) {
        Decision postSB = new Decision(Decision.Move.BET, SB);
        Decision postBB = new Decision(Decision.Move.RAISE, BB-SB);
        Player SBPlayer = playersStillPlaying.get(sbID);
        Player BBPlayer = playersStillPlaying.get(bbID);
        SBPlayer.act(postSB, 0);
        BBPlayer.act(postBB, BB-SB);
        gameController.setDecisionForClient(sbID, postSB);
        gameController.setDecisionForClient(bbID, postBB);
    }

    /**
     * Gets a valid decision from a player, and checks if it is valid. The decision is returned if the move is valid.
     *
     * @param playerToAct Player to get decision from
     * @param isPreflop true if the decision is made pre flop, else false
     * @return Player's valid decision
     */
    private Decision getValidDecisionFromPlayer(Player playerToAct, boolean isPreflop) {
        int errors = 0;
        System.out.println("Player to act " + playerToAct.getName() + " and currentbet is " + currentBet + " Biggest bet is " + biggestBet);
        long stackSize = playerToAct.getStackSize();

        while (true) {
            Decision decision = gameController.getDecisionFromClient(playerToAct.getID());
            switch (decision.move) {
                case FOLD: return decision;
                case CHECK: if (biggestBet == 0 || (isPreflop)) return decision; break;
                case CALL:
                    if (currentBet >= currentBB)
                        return decision;
                    else if (currentBet == 0) {
                        System.out.println("Player tried to call 0, returned check instead");
                        return new Decision(Decision.Move.CHECK);
                    }
                    break;

                case BET:
                    if (decision.size >=stackSize)
                        return new Decision(Decision.Move.BET, stackSize);
                    else if(decision.size >= currentBB)
                        return decision;
                    break;

                case RAISE:
                    if (decision.size + currentBet == stackSize) {
                        return decision;
                    }
                    else if (decision.size >= biggestBet)
                        return decision;

                    break;
            }

            System.out.println("Invalid move: " + playerToAct.getName() + " " + decision);
            System.exit(1);
        }
    }

    /**
     * Called when a new hand is about to start. Sets which player has dealer button, small and big blind.
     * Updates which players are still playing, and removes last hand's hole cards so that they are ready to get new ones.
     *
     * @param playersStillPlaying List of players still in the game
     */
    private void initializeNewHand(List<Player> playersStillPlaying) {
        this.pot = 0;

        dealerIndex = roundNumber % numberOfPlayers;
        if (numberOfPlayers == 2) {
            smallBlindIndex = roundNumber % numberOfPlayers;
        } else {
            smallBlindIndex = (roundNumber + 1) % numberOfPlayers;
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            Player player = players[(smallBlindIndex + i) % numberOfPlayers];
            if (player.stillPlaying()) {
                playersStillPlaying.add(player);
                player.setAmountPutOnTableThisBettingRound(0);
            }
        }

        bigBlindIndex = (smallBlindIndex + 1) % numberOfPlayers;
        holeCards = new HashMap<>();
        roundNumber++;
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

        Player p = new Player(name, startStack, table, ID);
        for (int i = 0; i < maxNumberOfPlayers; i++) {
            if (players[i] == null) {
                players[i] = p;
                numberOfPlayers++;
                break;
            }
        }

        stackSizes.put(ID, gamesettings.getStartStack());

        return table.addPlayer(p);
    }

    /**
     * Updates the stack sizes of each player after a hand is played.
     */
    private void updateStackSizes() {
        long minPutOnTable = Integer.MAX_VALUE;
        for (Player p : players)
            minPutOnTable = Math.min(minPutOnTable, p.getAmountPutOnTableThisBettingRound());

        for (Player p : players) {
            p.setAmountPutOnTableThisBettingRound(minPutOnTable);
            p.updateStackSize();
            stackSizes.put(p.getID(), p.getStackSize());
        }

        gameController.setStackSizes(stackSizes);
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
        gameController.setFlop(communityCards[0], communityCards[1], communityCards[2], pot);
    }

    /**
     * Tells the game controller to display the turn.
     */
    private void setTurn() {
        gameController.setTurn(communityCards[3], pot);
    }

    /**
     * Tells the game controller to display the river.
     */
    private void setRiver() {
        gameController.setRiver(communityCards[4], pot);
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
            p.setHand(cards[0], cards[1]);
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
            error = "Startstack must be a positive whole number";
        } else if(startBB < 0 || startSB < 0) {
            error = "All blinds must be positive whole numbers";
        } else if (startBB < startSB * 2) {
            error = "Big blind must be at least twice the size of the small blind";
        } else if(maxNumberOfPlayers < 2 || maxNumberOfPlayers > 8) {
            error = "Max number of players must be between 2-8";
        }

        return error;
    }

    /**
     * Updates the pot for each hand played
     */
    private void updatePot() {
        for (Player p : players) {
            pot += p.getAmountPutOnTableThisBettingRound();
            p.setAmountPutOnTableThisBettingRound(0);
        }
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
        // TODO next sprint: handle split
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
     * Displays each player's hole cards after a hand is over, and gives the pot to the winning player.
     */
    private void showDown() {
        List<Integer> IDStillPlaying = new ArrayList<>();
        for (Player p : playersStillPlaying) {
            IDStillPlaying.add(p.getID());
        }

        int winnerID = findWinnerID(IDStillPlaying);

        for (Player p : playersStillPlaying) {
            if (p.getID() == winnerID)
                p.incrementStack(pot);
        }
        updateStackSizes();
        updatePot();

        gameController.showDown(IDStillPlaying, winnerID, holeCards, pot);
        delay(5000);
    }
}
