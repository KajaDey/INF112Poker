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
    private int startSB, startBB;
    private long startStack;
    private Player [] players;

    public Game(GameSettings gamesettings) {
//        this.maxNumberOfPlayers = gamesettings.maxNumberOfPlayers;
        this.maxNumberOfPlayers = 2;
        this.table = new Table(maxNumberOfPlayers);
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.startSB = gamesettings.smallBlind;
        this.startBB = gamesettings.bigBlind;
        this.blindLevelDuration = gamesettings.levelDuration;
    }

    public void playGame() {
        numberOfPlayers = players.length;
        assert numberOfPlayers == maxNumberOfPlayers : "Incorrect number of players";

        int dealerIndex = 0;
        int smallBlindIndex = 0;
        int bigBlindIndex = 0;
        List<Player> playersStillPlaying = new ArrayList<>();

        /*
        set who has dealer button
        set who is bb and sb

        while no one has won
            assert bb and sb is paid
            deal cards to all players
            all players added to array of participants

            while cardsDisplayed < 5 -> play hand
                ask for decision from all participants, starting from player left for bb
                while not everyone agrees on bet
                    get decisions from everyone
                        if fold
                            removed from participants
                            update stack size
                if (not already 5 cards displayed)
                    display new card
        */


    }

    private void initializeNewRound(int dealerIndex, int smallBlindIndex, int bigBlindIndex, List<Player> playersStillPlaying) {
        for (int i = 0; i < numberOfPlayers; i++) {
            playersStillPlaying.add(players[i]);
        }

        if (numberOfPlayers == 2) {
            dealerIndex = 0;

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
                break;
            }
        }

        return table.addPlayer(p);
    }

    private boolean removePlayer(Player p) {
        // TODO implement
        return table.removePlayer(p);
    }

    public boolean isValid() {
        return startStack > 0 && startBB < startStack && startSB < startBB && maxNumberOfPlayers > 1 && maxNumberOfPlayers < 8;
    }
}
