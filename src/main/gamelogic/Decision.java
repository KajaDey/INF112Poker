package main.gamelogic;

/**
 * A decision made by the player, along with the size of a bet/raise
 */

public class Decision {
    public static enum Move {FOLD, CHECK, BET, RAISE, CALL};

    public final Move move;
    public final int size;

    /*
    Constructs a decision to raise or bet a certain amount
     */
    public Decision(Move move, int size) {
        assert move == Move.RAISE || move == Move.BET;

        this.move = move;
        this.size = size;
    }

    /*
    Constructs a decision to fold, check or call
     */
    public Decision(Move move) {
        assert move != Move.RAISE && move != Move.BET;

        this.move = move;
        this.size = -1;
    }
}
