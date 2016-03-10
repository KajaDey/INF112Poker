package main.java.gamelogic;

import main.java.gui.GameSettings;

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
        this.maxNumberOfPlayers = gamesettings.maxNumberOfPlayers;
        this.table = new Table(maxNumberOfPlayers);
        this.players = new Player[maxNumberOfPlayers];

        this.startStack = gamesettings.startStack;
        this.startSB = gamesettings.smallBlind;
        this.startBB = gamesettings.bigBlind;
        this.blindLevelDuration = gamesettings.levelDuration;
    }

    public void start() {


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

    public boolean isValid() {
        return startStack > 0 && startBB < startStack && startSB < startBB && maxNumberOfPlayers > 1 && maxNumberOfPlayers < 8;
    }
}
