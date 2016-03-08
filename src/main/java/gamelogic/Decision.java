package main.java.gamelogic;

/**
 * A decision made by the player, along with the size of a bet/raise
 */

public class Decision {
    public static enum Move {FOLD, CHECK, BET, RAISE, CALL};

    public final Move move;
    public final long size;

    /*
    Constructs a decision to raise or bet a certain amount
     */
    public Decision(Move move, long size) {
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Decision)) {
            return false;
        }
        Decision otherDecision = (Decision)other;
        if (move == Move.RAISE || move == Move.BET) {
            return this.move == otherDecision.move && this.size == otherDecision.size;
        }
        else {
            return this.move == otherDecision.move;
        }
    }

    @Override
    public String toString() {
        if (move == Move.RAISE || move == Move.BET) {
            return "Decision{ " + move + size + " }";
        }
        return "Decision{ " + move + " }";
    }
}
