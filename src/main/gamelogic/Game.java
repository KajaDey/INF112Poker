package main.gamelogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Game {

    private Table table;
    private int maxPlayers;
    private int blindLevelDuration;
    private int startSB, startBB;
    private int startStack;

    public Game(int maxPlayers, int startStack, int startSB, int startBB, int blindLevelDuration) {
        this.table = new Table(maxPlayers);

        this.maxPlayers = maxPlayers;
        this.startStack = startStack;
        this.startSB = startSB;
        this.startBB = startBB;
        this.blindLevelDuration = blindLevelDuration;
    }

    public boolean addPlayer(Player p) {
        return table.addPlayer(p);
    }


}
