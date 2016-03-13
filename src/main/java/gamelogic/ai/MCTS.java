package main.java.gamelogic.ai;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
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

    public MCTS(Pos startPosition, BiFunction<Pos, Move, Pos> doMove, Function<Pos, ArrayList<Move>> allLegalMoves,
                Function<Pos, Optional<Double>> getTerminalEvaluation) {
        this.startPosition = startPosition;
        this.totalSearches = 0;

        this.doMove = doMove;
        this.allLegalMoves = allLegalMoves;
        this.terminalEvaluation = getTerminalEvaluation;

        assert startPosition != null && doMove != null && allLegalMoves != null && getTerminalEvaluation != null;

        rootNode = new TreeNode(startPosition, true);
    }

    public void computeNodes(int nodes) {
        while (nodes > 0) {
            rootNode.select();
            nodes--;
        }
    }

    public void computeForMs(long milliseconds) {
        assert milliseconds > 100;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < milliseconds - 50) {
            computeNodes(1000);
        }

    }

    public class TreeNode {
        private double value;
        private int searches;

        public final Pos position;
        public final boolean maximizing;

        public List<Optional<TreeNode>> children;

        public final boolean isTerminal;
        public boolean allChildrenAreExplored = false;

        public TreeNode(Pos position, boolean maximizing) {

            this.position = position;
            this.maximizing = maximizing;

            assert position != null;

            value = 0.0;
            searches = 0;

            children = new ArrayList<>();

            ArrayList<Move> allMoves = allLegalMoves.apply(position);
            if (allMoves.size() == 0) {
                isTerminal = true;
            }
            else {
                isTerminal = false;
                for (Move m : allMoves) {
                    children.add(Optional.empty());
                }
            }

        }

        public void addValue(double value) {
            this.value += value;
            this.searches += 1;
            totalSearches += 1;
        }

        public double explorationValue() {
            double tempValue = !maximizing ? value : (value - 1) * -1;
            if (searches > 0) {
                return tempValue / searches + Math.sqrt(2) * Math.sqrt(Math.log(totalSearches) / searches);
            }
            else {
                return Double.POSITIVE_INFINITY;
            }
        }

        public double select() {

            if (allChildrenAreExplored) {
                TreeNode bestNode;
                if (maximizing) {/*
                    bestNode = children.stream()
                            .map(node -> node.get().explorationValue())
                            .max((child1, child2) -> Double.compare(explorationValue(), explorationValue())).get();
                */
                }
                Comparator<TreeNode> explorationValueComparator = (node1, node2) -> Double.compare(node1.explorationValue(), node2.explorationValue());

                bestNode = children.stream().map(Optional::get).max(explorationValueComparator).get();

                if (bestNode.isTerminal) {
                    Double eval = terminalEvaluation.apply((Pos) bestNode.position).get();
                    addValue(eval);
                    return eval;
                }
                else {
                    double eval = bestNode.select();
                    addValue(eval);
                    return eval;
                }
            }
            else {
                Optional<TreeNode> node = children.get((int)(Math.random()*children.size()));
                // Choose a new node
                while (node.isPresent()) {
                    node = children.get((int)(Math.random()*children.size()));
                }

                double eval = expand();
                addValue(eval);
                return eval;

            }
        }

        /**
         * Expands this node by creating a new node from a random
         */
        public double expand() {
            assert !isTerminal;

            int childIndex = (int)(Math.random()*children.size());
            while (children.get(childIndex).isPresent()) {
                childIndex = (int)(Math.random()*children.size());
            }

            List<Move> allMoves = allLegalMoves.apply(position);

            children.set(childIndex, Optional.of(new TreeNode(doMove.apply(position, allMoves.get(childIndex)), !this.maximizing)));

            if (children.stream().allMatch(Optional::isPresent)) {
                this.allChildrenAreExplored = true;
            }

            return children.get(childIndex).get().simulate();
            // Not necessary to add value because expand() already does this
        }

        public double simulate() {
            if (isTerminal) {
                return terminalEvaluation.apply(position).get();
            }
            List<Move> allMoves = allLegalMoves.apply(position);
            int randomChildIndex = (int)(Math.random() * children.size());
            TreeNode child = (new TreeNode(doMove.apply(position, allMoves.get(randomChildIndex)), !this.maximizing));

            return child.simulate();
        }


        public String toString() {
            return "Avg: " + value / searches + ", val: " + value + ", n: " + searches;
        }
    }
}