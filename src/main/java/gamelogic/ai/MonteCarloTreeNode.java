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

    private final Pos position;

    private final BiFunction<Pos, Move, Pos> doMove;
    private final Function<Pos, Optional<Double>> terminalEvaluation;
    private final Function<Pos, ArrayList<Move>> allLegalMoves;

    public MonteCarloTreeNode(Pos position, BiFunction<Pos, Move, Pos> doMove, Function<Pos, ArrayList<Move>> allLegalMoves,
                              Function<Pos, Optional<Double>> getTerminalEvaluation) {
        this.position = position;
        this.doMove = doMove;
        this.allLegalMoves = allLegalMoves;
        this.terminalEvaluation = getTerminalEvaluation;

        value = 0.0;
        searches = 0;

        children = new ArrayList<>();
        for (Move m : allLegalMoves.apply(position)) {
            children.add(Optional.empty());
        }
    }

    public double select() {
        Optional<MonteCarloTreeNode> node = children.get((int)(Math.random()*children.size()));

        if (node.isPresent()) {
            Optional<Double> eval = terminalEvaluation.apply((Pos)node.get().position);
            if (!eval.isPresent()) {
                return node.get().select();
            }
            else {
                return eval.get();
            }
        }
        else {
            return this.expand();
        }

    }

    public double expand() {
        assert !terminalEvaluation.apply(position).isPresent();
        List<Move> allMoves = allLegalMoves.apply(position);
        while (1 > 0) {
            int randomChildIndex = (int)(Math.random() * children.size());
            if (children.get(randomChildIndex).isPresent()) {
                continue;
            }
            children.set(randomChildIndex, Optional.of(new MonteCarloTreeNode<Pos, Move>(doMove.apply(position, allMoves.get(randomChildIndex)), doMove, allLegalMoves, terminalEvaluation)));
            return children.get(randomChildIndex).get().simulate();

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