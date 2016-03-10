package main.java.gamelogic.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by morten on 09.03.16.
 */
public class MCTS<Pos, Move> {

    public final TreeNode rootNode;

    public final Pos startPosition;
    public int totalSearches;

    public final BiFunction<Pos, Move, Pos> doMove;
    public final Function<Pos, Optional<Double>> terminalEvaluation;
    public final Function<Pos, ArrayList<Move>> allLegalMoves;


    public class TreeNode {
        private double value;
        private int searches;

        public final Pos position;

        public List<Optional<TreeNode>> children;

        public TreeNode(Pos position) {

            this.position = position;
            assert position != null;

            value = 0.0;
            searches = 0;

            children = new ArrayList<>();
            for (Move m : allLegalMoves.apply(position)) {
                children.add(Optional.empty());
            }
        }

        public void addValue(double value) {
            this.value += value;
            this.searches += 1;
            totalSearches += 1;
        }

        public double select() {
            Optional<TreeNode> node = children.get((int)(Math.random()*children.size()));

            if (node.isPresent()) {
                Optional<Double> eval = terminalEvaluation.apply((Pos)node.get().position);
                if (!eval.isPresent()) {
                    double trueEval = node.get().select();
                    addValue(trueEval);
                    return trueEval;
                }
                else {
                    addValue(eval.get());
                    return eval.get();
                }
            }
            else {
                // Not necessary to add value because expand() already does this
                return expand();
            }

        }

        public double expand() {
            assert !terminalEvaluation.apply(position).isPresent();
            List<Move> allMoves = allLegalMoves.apply(position);
            while (true) {
                int randomChildIndex = (int)(Math.random() * children.size());
                if (children.get(randomChildIndex).isPresent()) {
                    continue;
                }
                children.set(randomChildIndex, Optional.of(new TreeNode(doMove.apply(position, allMoves.get(randomChildIndex)))));

                double eval = children.get(randomChildIndex).get().simulate();
                this.value += eval;
                this.searches += 1;
                totalSearches += 1;
                return eval;
            }
        }

        public double simulate() {
            Optional<Double> evaluation = terminalEvaluation.apply(position);
            if (evaluation.isPresent()) {
                return evaluation.get();
            }
            List<Move> allMoves = allLegalMoves.apply(position);
            int randomChildIndex = (int)(Math.random() * children.size());
            TreeNode child = (new TreeNode(doMove.apply(position, allMoves.get(randomChildIndex))));

            return child.simulate();
        }


        public String toString() {
            return "Avg: " + value / searches + ", val: " + value + ", n: " + searches;
        }
    }

    public MCTS(Pos startPosition, BiFunction<Pos, Move, Pos> doMove, Function<Pos, ArrayList<Move>> allLegalMoves,
                Function<Pos, Optional<Double>> getTerminalEvaluation) {
        this.startPosition = startPosition;
        this.totalSearches = 0;

        this.doMove = doMove;
        this.allLegalMoves = allLegalMoves;
        this.terminalEvaluation = getTerminalEvaluation;

        assert startPosition != null && doMove != null && allLegalMoves != null && getTerminalEvaluation != null;

        rootNode = new TreeNode(startPosition);
    }




}