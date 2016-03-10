package main.java.gamelogic;

import main.java.gui.GameSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Game {

    private Table table;
    private int maxNumberOfPlayers;
    private int numberOfPlayers = 0;
    private int blindLevelDuration;
    private long startSB, startBB; // TODO: changed to long
    private long startStack;
    private Player [] players;
    private int dealerIndex = 0;
    private int bigBlindIndex = 0;
    private int roundNumber = 0;
    private int smallBlindIndex = 0;
    private long minimumBetThisRound = 0;

    public Game(GameSettings gamesettings) {
//        this.maxNumberOfPlayers = gamesettings.maxNumberOfPlayers;
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
        long SB = startSB;
        long BB = startBB;

        while (numberOfPlayers > 1) {
            initializeNewRound(playersStillPlaying);

            // Make sb and bb pay
            players[smallBlindIndex].setStackSize(players[smallBlindIndex].getStackSize() - SB);
            players[bigBlindIndex].setStackSize(players[bigBlindIndex].getStackSize() - BB);

            // Deal cards to all players, starting from player next to dealer
            Deck deck = new Deck();
            for (Player p : playersStillPlaying) {
                p.setHand(deck.draw().get(), deck.draw().get());
            }

            // TODO play hand:
            long pot = 0;
            long sidePot = 0;
            Decision previousDecision = null;
            boolean stillBetting = true;

            while (stillBetting) {
                for (Player p : playersStillPlaying) {
                    Decision decision = p.getLastDecision().get();

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
            /*
                ask for decision from all participants, starting from player left for bb
                while not everyone agrees on bet
                    get decisions from everyone
                        if fold
                            removed from participants
                            update stack size
                if (not already 5 cards displayed)
                    display new card
            */

            // TODO: check for blindraise
        }

    }

    private void initializeNewRound(List<Player> playersStillPlaying) {
        minimumBetThisRound = 0;
        dealerIndex = roundNumber%numberOfPlayers;
        if (numberOfPlayers == 2) {
            smallBlindIndex = roundNumber;
            bigBlindIndex = roundNumber + 1;
        }
        else {
            smallBlindIndex = (roundNumber + 1)%numberOfPlayers;
            bigBlindIndex = (roundNumber + 2)%numberOfPlayers;
        }

        for (int i = (dealerIndex + 1)%numberOfPlayers; i < numberOfPlayers; i++) {
            playersStillPlaying.add(players[i]);
        }
        for (int j = 0; j <= dealerIndex; j++) {
            playersStillPlaying.add(players[j]);
        }
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

    public boolean isValid() {
        return startStack > 0 && startBB < startStack && startSB < startBB && maxNumberOfPlayers > 1 && maxNumberOfPlayers < 8;
    }
}
