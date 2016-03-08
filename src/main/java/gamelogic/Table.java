package main.java.gamelogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristianrosland on 07.03.2016.
 */
public class Table {

    private List<Card> communityCards;
    private Player[] players;
    private int numberOfPlayers;

    public Table(int maxPlayers) {
        this.players = new Player[maxPlayers];
        numberOfPlayers = 0;
        communityCards = new ArrayList<Card>(5);
    }

    public List<Card> getCommunityCards() {
        return this.communityCards;
    }

    public boolean addPlayer(Player p) {
        if (numberOfPlayers >= players.length) {
            //TODO: Errorhandling
            return false;
        }

        players[numberOfPlayers++] = p;
        return true;
    }
}
