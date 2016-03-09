package main.java.gamelogic.ai;

import main.java.gamelogic.Card;
import main.java.gamelogic.Decision;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by morten on 09.03.16.
 */
public class GameState {
    private Player currentPlayer;
    private final int amountOfPlayers;
    private ArrayList<Player> players;

    private int bigBlindAmount;
    private int smallBlindAmount;
    private int minimumRaise;

    public GameState(int amountOfPlayers, ArrayList<Integer> positions, ArrayList<Long> stackSizes,
                     int smallBlindAmount, int bigBlindAmount) {

        this.amountOfPlayers = amountOfPlayers;
        this.smallBlindAmount = bigBlindAmount;
        this.bigBlindAmount = bigBlindAmount;

        players = new ArrayList<>();
        for (int i = 0; i < amountOfPlayers; i++) {
            players.add(new Player(i, positions.get(i), stackSizes.get(i)));
        }
        currentPlayer = players.get(0); 
    }

    /**
     *
     * @return
     */
    private ArrayList<Decision> allDecisions() {
        ArrayList<Decision> decisions = new ArrayList<>();
        decisions.add(new Decision(Decision.Move.FOLD));

        return decisions;
    }


}
