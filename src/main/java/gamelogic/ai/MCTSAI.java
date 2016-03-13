package main.java.gamelogic.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by morten on 11.03.16.
 */
public class MCTSAI extends SimpleAI {

    public MCTSAI(int playerId) {
        super(playerId);
    }

    public MCTSAI(int playerId, double contemptFactor) {
        super(playerId, contemptFactor);
    }

    public class TreeNode {
        final double[] values; // The expected profit for each player at this node
        final int player_to_move;
        private int searches;
        final List<TreeNode> children;

        public final boolean isTerminal;
        int numberOfExploredChildren = 0;

        public TreeNode(int player_to_move, boolean isTerminal) {
            this.values = new double[amountOfPlayers];
            this.player_to_move = player_to_move;
            this.isTerminal = isTerminal;
            children = new ArrayList<>();
        }
    }
}
