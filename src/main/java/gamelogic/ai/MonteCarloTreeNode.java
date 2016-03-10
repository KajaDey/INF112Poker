package main.java.gamelogic.ai;

import javax.management.monitor.MonitorSettingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by morten on 09.03.16.
 */
public class MonteCarloTreeNode<Pos, Move> {
    public List<Optional<MonteCarloTreeNode>> children;

    private double value;
    private int searches;

    public final Pos position;

    private final BiFunction<Pos, Move, Pos> doMove;
    private final Function<Pos, Optional<Double>> terminalEvaluation;
    public final Function<Pos, ArrayList<Move>> allLegalMoves;

    public MonteCarloTreeNode(Pos position, BiFunction<Pos, Move, Pos> doMove, Function<Pos, ArrayList<Move>> allLegalMoves,
                              Function<Pos, Optional<Double>> getTerminalEvaluation) {
        this.position = position;
        this.doMove = doMove;
        this.allLegalMoves = allLegalMoves;
        this.terminalEvaluation = getTerminalEvaluation;

        assert position != null && doMove != null && allLegalMoves != null && getTerminalEvaluation != null;

        value = 0.0;
        searches = 0;

        children = new ArrayList<>();
        for (Move m : allLegalMoves.apply(position)) {
            children.add(Optional.empty());
        }
    }

    public String toString() {
        return "Avg: " + value / searches + ", val: " + value + ", n: " + searches;
    }

    public double select() {
        Optional<MonteCarloTreeNode> node = children.get((int)(Math.random()*children.size()));

        if (node.isPresent()) {
            Optional<Double> eval = terminalEvaluation.apply((Pos)node.get().position);
            if (!eval.isPresent()) {
                double trueEval = node.get().select();
                this.value += trueEval;
                this.searches += 1;
                return trueEval;
            }
            else {
                this.value += eval.get();
                this.searches += 1;
                return eval.get();
            }
        }
        else {
            double eval = expand();
            //this.value += eval; Not necessary to increment because expand() already does this
            //this.searches += 1;
            return eval;
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
            children.set(randomChildIndex, Optional.of(new MonteCarloTreeNode<Pos, Move>(doMove.apply(position, allMoves.get(randomChildIndex)), doMove, allLegalMoves, terminalEvaluation)));

            double eval = children.get(randomChildIndex).get().simulate();
            this.value += eval;
            this.searches += 1;
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
        //Optional<MonteCarloTreeNode> node = children.get(randomChildIndex);
        MonteCarloTreeNode child = (new MonteCarloTreeNode<Pos, Move>(doMove.apply(position, allMoves.get(randomChildIndex)), doMove, allLegalMoves, terminalEvaluation));

        return child.simulate();
    }
}