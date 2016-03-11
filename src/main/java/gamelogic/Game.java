package main.java.gamelogic;

import main.java.gui.GameSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Game {

    //Controls
    private Table table;
    private Player [] players;
    private GameController gameController;

    //Settings
    private int maxNumberOfPlayers;
    private int numberOfPlayers = 0;
    private int blindLevelDuration;
    private long startSB, startBB;
    private long startStack;

    //Indexes
    private int dealerIndex = 0;
    private int bigBlindIndex = 0;
    private int smallBlindIndex = 0;

    //Rounds
    private int roundNumber = 0;
    private long minimumBetThisRound = 0;
    private long currentBet = 0;

    public Game(GameSettings gamesettings, GameController gameController) {
        this.gameController = gameController;

        this.maxNumberOfPlayers = 2;
        this.table = new Table(maxNumberOfPlayers);
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.startSB = (long) gamesettings.smallBlind;
        this.startBB = (long) gamesettings.bigBlind;
        this.blindLevelDuration = gamesettings.levelDuration;
    }

    public void playGame() {
        assert numberOfPlayers == maxNumberOfPlayers : "Incorrect number of players";
        // TODO set clock

        List<Player> playersStillPlaying = new ArrayList<>();
        long SB = startSB, BB = startBB;

        Handloop:
        while (numberOfPlayers > 1) {
            initializeNewRound(playersStillPlaying);

            // Deal cards to all players, starting from player next to dealer
            Deck deck = new Deck();
            for (Player p : playersStillPlaying) {
                Card card1 = deck.draw().get(), card2 = deck.draw().get();
                p.setHand(card1, card2);
                gameController.setHandForClient(p.getID(), card1, card2);
            }

            // TODO play hand:
            currentBet = BB;
            boolean bigBlindHasActed = false;

            // PREFLOP ROUND
            postBlinds(playersStillPlaying, SB, BB);

            int actingPlayerIndex = 2;

            while (true) {
                actingPlayerIndex %= numberOfPlayers;
                Player playerToAct = playersStillPlaying.get(actingPlayerIndex);
                Decision decision = getValidDecisionFromPlayer(playerToAct);
                playerToAct.act(decision, currentBet);

                if (actingPlayerIndex == bigBlindIndex)
                    bigBlindHasActed = true;

                System.out.println(playerToAct.getName() + ": " + decision);
                gameController.setDecisionForClient(playerToAct.getID(), decision);

                int nextPlayerToActIndex = (actingPlayerIndex + 1) % numberOfPlayers;
                Player nextPlayerToAct = playersStillPlaying.get(nextPlayerToActIndex);
                if (nextPlayerToAct.getLastDecision().isPresent()) {
                    if (nextPlayerToAct.getLastDecision().get().size == decision.size && bigBlindHasActed) {
                        System.out.println("We agree, bettinground over");
                    }
                }

                actingPlayerIndex++;
            }




            /*
            while (true) {
                for (Player p : playersStillPlaying) {
                    Decision decision = p.getLastDecision().get();
                    //gameController.getDecisionFromClient(p.ID())

                    switch (decision.move) {
                        case CHECK:
                            if (previousDecision != null && previousDecision.move != Decision.Move.CHECK) {
                                throw new RuntimeException("Illegal move");
                            } else if (previousDecision == null) {
                                continue;
                            }
                            break;
                        case FOLD:
                            playersStillPlaying.remove(p);
                            break;
                        case CALL:
                            if (decision.size != minimumBetThisRound) {
                                throw new RuntimeException("Illegal move");
                            }
                            pot += decision.size;
                            break;
                        case BET: break;
                        case RAISE: break;
                        // TODO: had to go to quiz....
                    }

                    previousDecision = decision; // when his turn ends
                }

            }
            // TODO: check for blindraise
            */

        }


    }

    private Decision getValidDecisionFromPlayer(Player playerToAct) {
        while (true) {
            Decision decision = gameController.getDecisionFromClient(playerToAct.getID());

            switch (decision.move) {
                case FOLD: return decision;
                case CALL: return decision;
                case CHECK: if (currentBet == 0) return decision; break;
                case RAISE: if (currentBet *2 <= decision.size) return decision; break;
            }

            System.out.println(playerToAct.getName() + " " + decision);
        }
    }

    private void initializeNewRound(List<Player> playersStillPlaying) {
        dealerIndex = roundNumber%numberOfPlayers;
        if (numberOfPlayers == 2) {
            smallBlindIndex = (roundNumber + 1) % numberOfPlayers;
        } else {
            smallBlindIndex = roundNumber %numberOfPlayers;
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

        return table.addPlayer(p);
    }

    private boolean removePlayer(Player p) {
        // TODO: anything else?
        numberOfPlayers--;
        return table.removePlayer(p);
    }

    private void postBlinds(List<Player> playersStillPlaying, Long SB, Long BB) {
        Decision postSB = new Decision(Decision.Move.BET, SB);
        Decision postBB = new Decision(Decision.Move.RAISE, BB-SB);
        Player SBPlayer = playersStillPlaying.get(0);
        Player BBPlayer = playersStillPlaying.get(1);
        SBPlayer.act(postSB, currentBet);
        BBPlayer.act(postBB, currentBet);
        gameController.setDecisionForClient(SBPlayer.getID(), postSB);
        gameController.setDecisionForClient(BBPlayer.getID(), postBB);
    }

    public boolean isValid() {
        return startStack > 0 && startBB < startStack && startSB < startBB && maxNumberOfPlayers > 1 && maxNumberOfPlayers < 8;
    }
}
