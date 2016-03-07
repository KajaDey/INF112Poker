package GameLogic;

/**
 * Created by kristianrosland on 07.03.2016.
 */

public class Decision {
    public static enum Move {FOLD, CHECK, BET, RAISE, CALL};

    public final Move move;
    public final int size;

    public Decision(Move move, int size) {
        assert move == Move.RAISE || move == Move.BET;

        this.move = move;
        this.size = size;
    }

    public Decision(Move move) {
        assert move != Move.RAISE && move != Move.BET;

        this.move = move;
        this.size = -1;
    }
}
