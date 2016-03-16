package gamelogic;

/**
 * A decision made by the player, along with the size of a bet/raise
 */

public class Decision {
    public static enum Move {FOLD, CHECK, BET, RAISE, CALL, BIG_BLIND, SMALL_BLIND, ALL_IN;
        public String toString() {
            return this == FOLD ? "Fold" : this == CHECK ? "Check" : this == BET ? "Bet" : this == CALL ? "Call":
                    this == RAISE ? "Raise" : this == BIG_BLIND ? "Big blind" : this == SMALL_BLIND ? "Small blind" : "All in";
        }
    } ;

    /**
     * FOLD, CHECK, CALL, ALL_IN has a size of -1
     * BET has a size of the total bet
     * RAISE has a size of the raise size (_not_ the total)
     * SMALL_BLIND/BIG_BLIND has a size of current sb/bb
     **/

    public final Move move;
    public final long size;

    /*
    Constructs a decision to raise or bet a certain amount or small/big blind
     */
    public Decision(Move move, long size) {
        assert move == Move.RAISE || move == Move.BET || move == Move.BIG_BLIND || move == Move.SMALL_BLIND;

        this.move = move;
        this.size = size;
    }

    /*
    Constructs a decision to fold, check, call or all in
     */
    public Decision(Move move) {
        assert move != Move.RAISE && move != Move.BET && move != Move.BIG_BLIND && move != Move.SMALL_BLIND;

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
            return "Decision{ " + move + " " + size + " }";
        }
        return "Decision{ " + move + " }";
    }
}
