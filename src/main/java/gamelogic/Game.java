package main.java.gamelogic;

import main.java.gui.GUIClient;
import main.java.gui.GameSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Long currentBet = 0L;
    private long pot = 0;
    private Map<Integer, Long> stackSizes;
    private Card [] communityCards;

    public Game(GameSettings gamesettings, GameController gameController) {
        this.gameController = gameController;
        this.gamesettings = gamesettings;

        this.maxNumberOfPlayers = 2;
        this.table = new Table(maxNumberOfPlayers);
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.startSB = (long) gamesettings.smallBlind;
        this.startBB = (long) gamesettings.bigBlind;
        this.blindLevelDuration = gamesettings.levelDuration;
        this.stackSizes = new HashMap<>();

    }

    public void playGame() {
        assert numberOfPlayers == maxNumberOfPlayers : "Incorrect number of players";
        // TODO set clock

        //Initiate clients
        gameController.initClients(gamesettings);

        currentSB = startSB;
        currentBB = startBB;

        boolean remainingPlayers = true;

        Handloop:
        while (true) {
            gameController.setStackSizes(stackSizes);
            delay(1000L);
            gameController.startNewHand();

            List<Player> playersStillPlaying = new ArrayList<>();
            initializeNewHand(playersStillPlaying);

            //Generate cards
            Deck deck = new Deck();
            dealHoleCards(deck, playersStillPlaying);
            communityCards = generateCommunityCards(deck);

            currentBet = currentBB;

            // PREFLOP ROUND
            postBlinds(playersStillPlaying, smallBlindIndex, bigBlindIndex, currentSB, currentBB);

            remainingPlayers = bettingRound(playersStillPlaying, 2);
            if (!remainingPlayers) { continue; }

            setFlop();

            currentBet = 0L;
            remainingPlayers = bettingRound(playersStillPlaying, 0);
            if (!remainingPlayers) { continue; }

            setTurn();

            currentBet = 0L;
            remainingPlayers = bettingRound(playersStillPlaying, 0);
            if (!remainingPlayers) { continue; }

            setRiver();

            currentBet = 0L;
            remainingPlayers = bettingRound(playersStillPlaying, 0);
            if (!remainingPlayers) { continue; }

            //SHOWDOWN
            System.out.println("SHOWDOWN");
            if (playersStillPlaying.size() > 1) {
                for (Player player : playersStillPlaying) {
                    System.out.println("Player " + player.getID() + player.cardsOnHand());
                }
            }

            ArrayList<Integer> stillPlaying = new ArrayList<Integer>();
            for (Player p : playersStillPlaying)
                stillPlaying.add(p.getID());

            gameController.showDown(stillPlaying, 0);
        }
    }

    private boolean bettingRound(List<Player> playersStillPlaying, int actingPlayerIndex) {
        int numberOfActedPlayers = 0;

        while (true) {
            actingPlayerIndex %= numberOfPlayers;
            Player playerToAct = playersStillPlaying.get(actingPlayerIndex);
            Decision decision = getValidDecisionFromPlayer(playerToAct);
            playerToAct.act(decision, currentBet);

            System.out.println(playerToAct.getName() + " acted: " + decision);
            gameController.setDecisionForClient(playerToAct.getID(), decision);

            switch(decision.move) {
                case RAISE:case BET:
                    numberOfActedPlayers = 1;
                    currentBet += decision.size;
                    break;
                case FOLD: playersStillPlaying.remove(playerToAct); break;
                default: numberOfActedPlayers++;
            }

            //If only one player left in hand
            if (playersStillPlaying.size() <= 1) {
                System.out.println("Only one player left, hand over");
                return false;
            }

            //If all players have acted
            if (numberOfActedPlayers == playersStillPlaying.size()) {
                System.out.println("Bettinground finished, hand continues");
                updateStackSizes();
                updatePot();
                return true;
            }

            actingPlayerIndex++;
        }
    }

    private void postBlinds(List<Player> playersStillPlaying, int sbID, int bbID, Long SB, Long BB) {
        Decision postSB = new Decision(Decision.Move.BET, SB);
        Decision postBB = new Decision(Decision.Move.RAISE, BB-SB);
        Player SBPlayer = playersStillPlaying.get(sbID);
        Player BBPlayer = playersStillPlaying.get(bbID);
        SBPlayer.act(postSB, currentBet);
        BBPlayer.act(postBB, currentBet);
        gameController.setDecisionForClient(sbID, postSB);
        gameController.setDecisionForClient(bbID, postBB);
    }

    private Decision getValidDecisionFromPlayer(Player playerToAct) {
        int errors = 0;
        System.out.println("Player to act " + playerToAct.getName() + " and currentbet is " + currentBet);

        while (true) {
            Decision decision = gameController.getDecisionFromClient(playerToAct.getID());

            switch (decision.move) {
                //TODO: Check that player has enough chips for this decision
                case FOLD: return decision;
                case CALL:
                    if (currentBet >= currentBB)
                        return decision;
                    else if (currentBet == 0) {
                        System.out.println("Player tried to call 0, returned check instead");
                        return new Decision(Decision.Move.CHECK);
                    }
                    break;
                case BET: if (decision.size >= currentBB) return decision; break;
                case CHECK: if (currentBet == 0) return decision; break;
                case RAISE: if (decision.size >= currentBet) return decision; break;
            }

            System.out.println("Invalid move: " + playerToAct.getName() + " " + decision);

            if (errors++ == 10) System.exit(0); // <-- superhack
        }
    }

    private void initializeNewHand(List<Player> playersStillPlaying) {
        this.pot = 0;

        dealerIndex = roundNumber%numberOfPlayers;
        if (numberOfPlayers == 2) {
            smallBlindIndex = roundNumber %numberOfPlayers;
        } else {
            smallBlindIndex = (roundNumber + 1) % numberOfPlayers;
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            Player p = players[(smallBlindIndex+i) % numberOfPlayers];
            if (p.stillPlaying())
                playersStillPlaying.add(p);
        }

        bigBlindIndex = (smallBlindIndex+1) % numberOfPlayers;
    }

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

    private void updateStackSizes() {
        for (Player p : players) {
            stackSizes.put(p.getID(), p.getStackSize());
        }

        gameController.setStackSizes(stackSizes);
    }

    private void delay(Long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(Exception e) {
            System.out.println("Error when sleeping thread " + Thread.currentThread());
        }
    }

    private void setFlop() {
        gameController.setFlop(communityCards[0], communityCards[1], communityCards[2], pot);
    }

    private void setTurn() {
        gameController.setTurn(communityCards[3], pot);
    }

    private void setRiver() {
        gameController.setRiver(communityCards[4], pot);
    }

    private void dealHoleCards(Deck deck, List<Player> playersStillPlaying) {
        for (Player p : playersStillPlaying) {
            Card card1 = deck.draw().get(), card2 = deck.draw().get();
            p.setHand(card1, card2);
            gameController.setHandForClient(p.getID(), card1, card2);
        }
    }

    public boolean isValid() {
        return startStack > 0 && startBB < startStack && startSB < startBB && maxNumberOfPlayers > 1 && maxNumberOfPlayers < 8;
    }

    private void updatePot() {
        for (Player p : players) {
            pot += p.getAmountPutOnTableThisBettingRound();
            p.setAmountPutOnTableThisBettingRound(0L);
        }
    }

    private Card[] generateCommunityCards(Deck deck) {
        Card [] commCards = new Card[5];
        for (int i = 0; i < commCards.length; i++)
            commCards[i] = deck.draw().get();
        return commCards;
    }

}
