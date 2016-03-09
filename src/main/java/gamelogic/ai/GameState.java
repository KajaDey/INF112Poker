package main.java.gamelogic.ai;

import main.java.gamelogic.Card;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by morten on 09.03.16.
 */
public class GameState {
    private int currentPlayer;
    private final int amountOfPlayers;
    private ArrayList<ArrayList<Optional<Card>>> holeCards; // Hole cards for all players

    private int bigBlindAmount;
    private int smallBlindAmount;
    private ArrayList<Integer> positions; // Player positions on the board, indexed by playerID

    public GameState(int amountOfPlayers) {
        this.amountOfPlayers = amountOfPlayers;
    }
}
